package com.wasac.billing.controller;

import com.wasac.billing.entity.Bill;
import com.wasac.billing.entity.Tariff;
import com.wasac.billing.enums.BillStatus;
import com.wasac.billing.exception.BusinessException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.BillRepository;
import com.wasac.billing.repository.TariffRepository;
import com.wasac.billing.service.AuditLogService;
import com.wasac.billing.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
public class PenaltyController {

    private final BillRepository billRepository;
    private final TariffRepository tariffRepository;
    private final AuditLogService auditLogService;
    private final EmailService emailService;

    public PenaltyController(
            BillRepository billRepository,
            TariffRepository tariffRepository,
            AuditLogService auditLogService,
            EmailService emailService) {
        this.billRepository = billRepository;
        this.tariffRepository = tariffRepository;
        this.auditLogService = auditLogService;
        this.emailService = emailService;
    }

    @PostMapping("/{billId}/apply-penalty")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Transactional
    public ResponseEntity<Map<String, Object>> applyPenalty(
            @PathVariable Long billId,
            @RequestParam(defaultValue = "false") boolean force) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id " + billId));

        if (bill.getStatus() == BillStatus.PAID) {
            throw new BusinessException("Cannot apply penalty to a fully paid bill");
        }
        if (!force && !bill.getDueDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Penalty can only be applied after the bill due date");
        }
        if (bill.getPenaltyAmount() != null && bill.getPenaltyAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Penalty has already been applied to this bill");
        }
        if (bill.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Cannot apply penalty because the outstanding balance is zero");
        }

        Tariff tariff = tariffRepository
                .findFirstByMeterTypeAndEffectiveFromLessThanEqualAndActiveTrueOrderByVersionDesc(
                        bill.getMeter().getMeterType(), LocalDate.now())
                .orElseThrow(() -> new BusinessException(
                        "No active tariff found for meter type " + bill.getMeter().getMeterType()));

        BigDecimal penaltyAmount = bill.getOutstandingBalance()
                .multiply(tariff.getPenaltyPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        bill.setPenaltyAmount(penaltyAmount);
        bill.setTotalAmount(bill.getTotalAmount().add(penaltyAmount).setScale(2, RoundingMode.HALF_UP));
        bill.setOutstandingBalance(bill.getOutstandingBalance().add(penaltyAmount).setScale(2, RoundingMode.HALF_UP));
        bill.setStatus(BillStatus.OVERDUE);

        Bill saved = billRepository.save(bill);

        String message = "Dear " + saved.getCustomer().getFullNames()
                + ",\nA late-payment penalty of " + penaltyAmount.toPlainString()
                + " FRW has been applied to bill " + saved.getBillNumber()
                + ". New outstanding balance: " + saved.getOutstandingBalance().toPlainString() + " FRW.";
        emailService.sendNotificationEmail(saved.getCustomer().getEmail(), "Utility Bill Penalty Applied", message);

        auditLogService.log("APPLY_PENALTY", "Bill", saved.getId(),
                "Applied penalty " + penaltyAmount + " to bill " + saved.getBillNumber());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("billId", saved.getId());
        response.put("billNumber", saved.getBillNumber());
        response.put("penaltyPercentage", tariff.getPenaltyPercentage());
        response.put("penaltyAmount", saved.getPenaltyAmount());
        response.put("totalAmount", saved.getTotalAmount());
        response.put("outstandingBalance", saved.getOutstandingBalance());
        response.put("status", saved.getStatus());
        return ResponseEntity.ok(response);
    }
}
