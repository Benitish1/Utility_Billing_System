package com.wasac.billing.dto;

import com.wasac.billing.enums.BillStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Request and response models for bill generation and bill lookup responses.
 */
public final class BillingDtos {
    private BillingDtos() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateBillRequest {
        @NotNull(message = "Reading ID is required")
        @jakarta.validation.constraints.Positive(message = "Reading ID must be positive")
        private Long readingId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillResponse {
        private Long id;
        private String billNumber;
        private Long customerId;
        private String customerName;
        private Long meterId;
        private String meterNumber;
        private Long readingId;
        private BigDecimal amountBeforeTax;
        private BigDecimal taxAmount;
        private BigDecimal penaltyAmount;
        private BigDecimal totalAmount;
        private BigDecimal amountPaid;
        private BigDecimal outstandingBalance;
        private Integer billingMonth;
        private Integer billingYear;
        private LocalDate dueDate;
        private BillStatus status;
        private LocalDateTime generatedAt;
    }
}
