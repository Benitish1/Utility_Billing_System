package com.wasac.billing.serviceImpl;

import com.wasac.billing.dto.BillingDtos;
import com.wasac.billing.entity.*;
import com.wasac.billing.enums.BillStatus;
import com.wasac.billing.enums.EntityStatus;
import com.wasac.billing.enums.NotificationStatus;
import com.wasac.billing.enums.Role;
import com.wasac.billing.exception.AccessDeniedException;
import com.wasac.billing.exception.BusinessException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.*;
import com.wasac.billing.service.AuditLogService;
import com.wasac.billing.service.BillingService;
import com.wasac.billing.service.EmailService;
import com.wasac.billing.utils.AppUtils;
import com.wasac.billing.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final BillRepository billRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final TariffRepository tariffRepository;

    private final CustomerRepository customerRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public BillingDtos.BillResponse generateBill(BillingDtos.GenerateBillRequest request) {
        MeterReading reading = meterReadingRepository.findById(request.getReadingId())
                .orElseThrow(() -> new ResourceNotFoundException("Meter reading not found"));

        if (billRepository.existsByReadingId(reading.getId())) {
            throw new BusinessException("Bill already generated for this reading");
        }

        Meter meter = reading.getMeter();
        Customer customer = meter.getCustomer();

        if (customer.getStatus() != EntityStatus.ACTIVE) {
            throw new BusinessException("Inactive customers cannot receive bills");
        }
        if (meter.getStatus() != EntityStatus.ACTIVE) {
            throw new BusinessException("Inactive meters cannot be billed");
        }

        YearMonth billingPeriod = YearMonth.of(reading.getBillingYear(), reading.getBillingMonth());
        LocalDate billingPeriodEnd = billingPeriod.atEndOfMonth();
        Tariff tariff = tariffRepository
                .findFirstByMeterTypeAndEffectiveFromLessThanEqualAndActiveTrueOrderByVersionDesc(
                        meter.getMeterType(), billingPeriodEnd)
                .orElseThrow(() -> new BusinessException(
                        "No active tariff found for meter type " + meter.getMeterType()
                                + " for billing period " + reading.getBillingMonth() + "/" + reading.getBillingYear()
                                + ". Ensure an active tariff exists with effectiveFrom on or before "
                                + billingPeriodEnd));

        BigDecimal amountBeforeTax = reading.getUnitsConsumed()
                .multiply(tariff.getPricePerUnit())
                .add(tariff.getFixedCharge())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxAmount = amountBeforeTax
                .multiply(tariff.getVatPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal penaltyAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = amountBeforeTax.add(taxAmount).add(penaltyAmount).setScale(2, RoundingMode.HALF_UP);

        long sequence = billRepository.countByBillingYearAndBillingMonth(
                reading.getBillingYear(), reading.getBillingMonth()) + 1;
        String billNumber = AppUtils.generateBillNumber(reading.getBillingYear(), reading.getBillingMonth(), sequence);

        Bill bill = Bill.builder()
                .billNumber(billNumber)
                .customer(customer)
                .meter(meter)
                .reading(reading)
                .amountBeforeTax(amountBeforeTax)
                .taxAmount(taxAmount)
                .penaltyAmount(penaltyAmount)
                .totalAmount(totalAmount)
                .amountPaid(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .outstandingBalance(totalAmount)
                .billingMonth(reading.getBillingMonth())
                .billingYear(reading.getBillingYear())
                .dueDate(LocalDate.now().plusDays(30))
                .status(BillStatus.PENDING)
                .build();

        Bill saved = billRepository.saveAndFlush(bill);
        String message = AppUtils.buildNotificationMessage(
                saved.getCustomer().getFullNames(),
                saved.getBillingMonth(),
                saved.getBillingYear(),
                saved.getTotalAmount().toPlainString());
        createFallbackNotification(saved, message);
        emailService.sendNotificationEmail(saved.getCustomer().getEmail(), "Utility Bill Generated", message);
        auditLogService.log("GENERATE_BILL", "Bill", saved.getId(),
                "Generated bill " + saved.getBillNumber() + " for customer " + saved.getCustomer().getEmail());
        return toResponse(saved);
    }

    private void createFallbackNotification(Bill bill, String message) {
        boolean alreadyCreated = notificationRepository.findByCustomerIdOrderBySentAtDesc(bill.getCustomer().getId())
                .stream()
                .anyMatch(notification -> notification.getMessage().equals(message));

        if (!alreadyCreated) {
            Notification notification = Notification.builder()
                    .customer(bill.getCustomer())
                    .message(message)
                    .status(NotificationStatus.SENT)
                    .build();
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public BillingDtos.BillResponse resendBillEmail(Long id) {
        Bill bill = findBill(id);
        String message = AppUtils.buildNotificationMessage(
                bill.getCustomer().getFullNames(),
                bill.getBillingMonth(),
                bill.getBillingYear(),
                bill.getTotalAmount().toPlainString());

        createFallbackNotification(bill, message);
        emailService.sendNotificationEmail(bill.getCustomer().getEmail(), "Utility Bill Generated", message);
        auditLogService.log("RESEND_BILL_EMAIL", "Bill", bill.getId(),
                "Resent bill email " + bill.getBillNumber() + " to " + bill.getCustomer().getEmail());

        return toResponse(bill);
    }

    @Override
    @Transactional
    public BillingDtos.BillResponse approveBill(Long id) {
        Bill bill = findBill(id);

        if (bill.getStatus() == BillStatus.APPROVED) {
            throw new BusinessException("Bill is already approved");
        }
        if (bill.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Bill cannot be approved before the customer clears the outstanding balance");
        }
        if (bill.getStatus() != BillStatus.PAID) {
            throw new BusinessException("Only fully paid bills can be approved by finance");
        }

        bill.setStatus(BillStatus.APPROVED);
        Bill saved = billRepository.save(bill);

        String message = "Dear " + saved.getCustomer().getFullNames()
                + ",\nYour bill " + saved.getBillNumber()
                + " has been approved by finance. Thank you for your payment.";
        createFallbackNotification(saved, message);
        emailService.sendNotificationEmail(saved.getCustomer().getEmail(), "Utility Bill Approved", message);
        auditLogService.log("APPROVE_BILL", "Bill", saved.getId(),
                "Approved fully paid bill " + saved.getBillNumber());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BillingDtos.BillResponse getById(Long id) {
        Bill bill = findBill(id);
        assertCustomerAccess(bill);
        return toResponse(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public BillingDtos.BillResponse getByBillNumber(String billNumber) {
        Bill bill = billRepository.findByBillNumber(billNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with number " + billNumber));
        assertCustomerAccess(bill);
        return toResponse(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingDtos.BillResponse> getAll() {
        return billRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingDtos.BillResponse> getMyBills() {
        User user = SecurityUtils.getCurrentUser();
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No customer profile linked to your account"));
        return billRepository.findByCustomerId(customer.getId()).stream().map(this::toResponse).toList();
    }

    private void assertCustomerAccess(Bill bill) {
        User user = SecurityUtils.getCurrentUser();
        if (user.getRole() == Role.ROLE_CUSTOMER) {
            customerRepository.findByUserId(user.getId()).ifPresentOrElse(
                    customer -> {
                        if (!bill.getCustomer().getId().equals(customer.getId())) {
                            throw new AccessDeniedException("You can only view your own bills");
                        }
                    },
                    () -> {
                        throw new AccessDeniedException("No customer profile linked to your account");
                    });
        }
    }

    private Bill findBill(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id " + id));
    }

    private BillingDtos.BillResponse toResponse(Bill bill) {
        return BillingDtos.BillResponse.builder()
                .id(bill.getId())
                .billNumber(bill.getBillNumber())
                .customerId(bill.getCustomer().getId())
                .customerName(bill.getCustomer().getFullNames())
                .meterId(bill.getMeter().getId())
                .meterNumber(bill.getMeter().getMeterNumber())
                .readingId(bill.getReading().getId())
                .amountBeforeTax(bill.getAmountBeforeTax())
                .taxAmount(bill.getTaxAmount())
                .penaltyAmount(bill.getPenaltyAmount())
                .totalAmount(bill.getTotalAmount())
                .amountPaid(bill.getAmountPaid())
                .outstandingBalance(bill.getOutstandingBalance())
                .billingMonth(bill.getBillingMonth())
                .billingYear(bill.getBillingYear())
                .dueDate(bill.getDueDate())
                .status(bill.getStatus())
                .generatedAt(bill.getGeneratedAt())
                .build();
    }
}
