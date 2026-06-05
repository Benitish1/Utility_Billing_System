package com.wasac.billing.serviceImpl;

import com.wasac.billing.dto.PaymentDtos;
import com.wasac.billing.entity.Bill;
import com.wasac.billing.entity.Customer;
import com.wasac.billing.entity.Notification;
import com.wasac.billing.entity.Payment;
import com.wasac.billing.entity.User;
import com.wasac.billing.enums.BillStatus;
import com.wasac.billing.enums.NotificationStatus;

import com.wasac.billing.exception.BusinessException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.BillRepository;
import com.wasac.billing.repository.CustomerRepository;
import com.wasac.billing.repository.NotificationRepository;
import com.wasac.billing.repository.PaymentRepository;
import com.wasac.billing.service.AuditLogService;
import com.wasac.billing.service.EmailService;
import com.wasac.billing.service.PaymentService;
import com.wasac.billing.utils.AppUtils;
import com.wasac.billing.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Handles payment recording, overpayment prevention, balance updates, and payment emails.
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final JdbcTemplate jdbcTemplate;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public PaymentDtos.PaymentResponse recordPayment(PaymentDtos.PaymentRequest request) {
        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id " + request.getBillId()));

        if (bill.getStatus() == BillStatus.PAID) {
            throw new BusinessException("Bill is already fully paid");
        }
        if (request.getAmountPaid().compareTo(bill.getOutstandingBalance()) > 0) {
            throw new BusinessException("Payment amount exceeds outstanding balance. Overpayment is not allowed");
        }

        String transactionRef = request.getTransactionReference();
        if (transactionRef == null || transactionRef.isBlank()) {
            transactionRef = AppUtils.generateTransactionReference();
        }
        if (paymentRepository.existsByTransactionReference(transactionRef)) {
            throw new BusinessException("Transaction reference already exists", HttpStatus.CONFLICT);
        }

        User financeUser = SecurityUtils.getCurrentUser();
        Payment payment = Payment.builder()
                .bill(bill)
                .amountPaid(request.getAmountPaid())
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(request.getPaymentDate())
                .receivedBy(financeUser)
                .transactionReference(transactionRef)
                .build();
        Payment saved = paymentRepository.save(payment);

        BigDecimal newAmountPaid = bill.getAmountPaid().add(request.getAmountPaid()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newBalance = bill.getTotalAmount().subtract(newAmountPaid).setScale(2, RoundingMode.HALF_UP);

        bill.setAmountPaid(newAmountPaid);
        bill.setOutstandingBalance(newBalance);

        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            bill.setStatus(BillStatus.PAID);
            notifyFullPayment(bill);
        } else {
            bill.setStatus(BillStatus.PARTIALLY_PAID);
        }
        billRepository.save(bill);
        auditLogService.log("RECORD_PAYMENT", "Payment", saved.getId(),
                "Recorded payment " + saved.getAmountPaid() + " for bill " + bill.getBillNumber()
                        + ". Bill status is " + bill.getStatus());

        return toResponse(saved);
    }

    private void notifyFullPayment(Bill bill) {
        try {
            jdbcTemplate.update("CALL process_full_payment(?)", bill.getId());
        } catch (Exception ex) {
            createFallbackNotification(bill);
        }
        String message = AppUtils.buildNotificationMessage(
                bill.getCustomer().getFullNames(),
                bill.getBillingMonth(),
                bill.getBillingYear(),
                bill.getTotalAmount().toPlainString());
        emailService.sendNotificationEmail(bill.getCustomer().getEmail(), "Payment Confirmation", message);
    }

    private void createFallbackNotification(Bill bill) {
        String message = AppUtils.buildNotificationMessage(
                bill.getCustomer().getFullNames(),
                bill.getBillingMonth(),
                bill.getBillingYear(),
                bill.getTotalAmount().toPlainString());

        if (notificationRepository.findByCustomerIdOrderBySentAtDesc(bill.getCustomer().getId())
                .stream().noneMatch(n -> n.getMessage().equals(message))) {
            Notification notification = Notification.builder()
                    .customer(bill.getCustomer())
                    .message(message)
                    .status(NotificationStatus.SENT)
                    .build();
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDtos.PaymentResponse> getAll() {
        return paymentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDtos.PaymentResponse> getByBill(Long billId) {
        return paymentRepository.findByBillId(billId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDtos.PaymentResponse> getMyPayments() {
        User user = SecurityUtils.getCurrentUser();
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No customer profile linked to your account"));
        return paymentRepository.findByBillCustomerId(customer.getId()).stream().map(this::toResponse).toList();
    }

    private PaymentDtos.PaymentResponse toResponse(Payment payment) {
        return PaymentDtos.PaymentResponse.builder()
                .id(payment.getId())
                .billId(payment.getBill().getId())
                .billNumber(payment.getBill().getBillNumber())
                .amountPaid(payment.getAmountPaid())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDate(payment.getPaymentDate())
                .receivedBy(payment.getReceivedBy().getId())
                .transactionReference(payment.getTransactionReference())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
