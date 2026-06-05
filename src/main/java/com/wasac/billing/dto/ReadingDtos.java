package com.wasac.billing.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Request and response models for monthly meter readings captured by operators.
 */
public final class ReadingDtos {
    private ReadingDtos() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReadingRequest {
        @NotNull(message = "Meter ID is required")
        @Positive(message = "Meter ID must be positive")
        private Long meterId;

        @NotNull(message = "Previous reading is required")
        @DecimalMin(value = "0.00", message = "Previous reading cannot be negative")
        private BigDecimal previousReading;

        @NotNull(message = "Current reading is required")
        @DecimalMin(value = "0.01", message = "Current reading must be positive")
        private BigDecimal currentReading;

        @NotNull(message = "Reading date is required")
        @PastOrPresent(message = "Reading date cannot be in the future")
        private LocalDate readingDate;

        @NotNull(message = "Billing month is required")
        @Min(value = 1, message = "Billing month must be between 1 and 12")
        @Max(value = 12, message = "Billing month must be between 1 and 12")
        private Integer billingMonth;

        @NotNull(message = "Billing year is required")
        @Min(value = 2020, message = "Billing year must be 2020 or later")
        private Integer billingYear;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadingResponse {
        private Long id;
        private Long meterId;
        private String meterNumber;
        private BigDecimal previousReading;
        private BigDecimal currentReading;
        private BigDecimal unitsConsumed;
        private LocalDate readingDate;
        private Integer billingMonth;
        private Integer billingYear;
        private Long createdBy;
        private LocalDateTime createdAt;
    }
}
