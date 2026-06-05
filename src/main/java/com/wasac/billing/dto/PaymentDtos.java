package com.wasac.billing.dto;

import com.wasac.billing.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Request and response models for partial and full bill payments.
 */
public final class PaymentDtos {
    private PaymentDtos() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentRequest {
        @NotNull(message = "Bill ID is required")
        @Positive(message = "Bill ID must be positive")
        private Long billId;

        @NotNull(message = "Amount paid is required")
        @DecimalMin(value = "1.00", message = "Amount paid must be at least 1 FRW")
        private BigDecimal amountPaid;

        @NotNull(message = "Payment method is required")
        private PaymentMethod paymentMethod;

        @NotNull(message = "Payment date is required")
        @PastOrPresent(message = "Payment date cannot be in the future")
        private LocalDate paymentDate;

        @Size(max = 80, message = "Transaction reference cannot exceed 80 characters")
        @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "Transaction reference may contain letters, numbers, dots, underscores, and hyphens only")
        private String transactionReference;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResponse {
        private Long id;
        private Long billId;
        private String billNumber;
        private BigDecimal amountPaid;
        private PaymentMethod paymentMethod;
        private LocalDate paymentDate;
        private Long receivedBy;
        private String transactionReference;
        private LocalDateTime createdAt;
    }
}
