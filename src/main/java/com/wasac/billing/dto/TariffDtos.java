package com.wasac.billing.dto;

import com.wasac.billing.enums.MeterType;
import com.wasac.billing.enums.TariffType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request and response models for versioned tariff, tax, and penalty configuration.
 */
public final class TariffDtos {
    private TariffDtos() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TariffRequest {
        @NotNull(message = "Meter type is required")
        private MeterType meterType;

        @NotNull(message = "Tariff type is required")
        private TariffType tariffType;

        @NotNull(message = "Price per unit is required")
        @DecimalMin(value = "0.01", message = "Price per unit must be greater than zero")
        private BigDecimal pricePerUnit;

        @NotNull(message = "Fixed charge is required")
        @DecimalMin(value = "0.00", message = "Fixed charge cannot be negative")
        private BigDecimal fixedCharge;

        @NotNull(message = "VAT percentage is required")
        @DecimalMin(value = "0.00", message = "VAT percentage cannot be negative")
        @DecimalMax(value = "100.00", message = "VAT percentage cannot exceed 100")
        private BigDecimal vatPercentage;

        @NotNull(message = "Penalty percentage is required")
        @DecimalMin(value = "0.00", message = "Penalty percentage cannot be negative")
        @DecimalMax(value = "100.00", message = "Penalty percentage cannot exceed 100")
        private BigDecimal penaltyPercentage;

        @NotNull(message = "Version is required")
        @Positive(message = "Version must be positive")
        private Integer version;

        @NotNull(message = "Effective date is required")
        @FutureOrPresent(message = "New tariffs must apply to current or future billing cycles")
        private LocalDate effectiveFrom;

        @NotNull(message = "Active flag is required")
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TariffResponse {
        private Long id;
        private MeterType meterType;
        private TariffType tariffType;
        private BigDecimal pricePerUnit;
        private BigDecimal fixedCharge;
        private BigDecimal vatPercentage;
        private BigDecimal penaltyPercentage;
        private Integer version;
        private LocalDate effectiveFrom;
        private boolean active;
    }
}
