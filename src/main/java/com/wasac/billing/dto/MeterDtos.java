package com.wasac.billing.dto;

import com.wasac.billing.enums.EntityStatus;
import com.wasac.billing.enums.MeterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

/**
 * Request and response models for assigning utility meters to customers.
 */
public final class MeterDtos {
    private MeterDtos() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeterRequest {
        @NotBlank(message = "Meter number is required")
        @Size(min = 3, max = 50, message = "Meter number must be between 3 and 50 characters")
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "Meter number must contain uppercase letters, numbers, and hyphens only")
        private String meterNumber;

        @NotNull(message = "Meter type is required")
        private MeterType meterType;

        @NotNull(message = "Installation date is required")
        @PastOrPresent(message = "Installation date cannot be in the future")
        private LocalDate installationDate;

        @NotNull(message = "Status is required")
        private EntityStatus status;

        @NotNull(message = "Customer ID is required")
        @Positive(message = "Customer ID must be positive")
        private Long customerId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterResponse {
        private Long id;
        private String meterNumber;
        private MeterType meterType;
        private LocalDate installationDate;
        private EntityStatus status;
        private Long customerId;
        private String customerName;
    }
}
