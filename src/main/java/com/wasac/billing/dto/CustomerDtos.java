package com.wasac.billing.dto;

import com.wasac.billing.enums.EntityStatus;
import com.wasac.billing.utils.RwandaNationalId;
import com.wasac.billing.utils.RwandaPhone;
import com.wasac.billing.utils.ValidEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Request and response models for customer registration and profile management.
 */
public final class CustomerDtos {
    private CustomerDtos() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerRequest {
        @NotBlank(message = "Customer full names are required")
        @Size(min = 3, max = 120, message = "Full names must be between 3 and 120 characters")
        private String fullNames;

        @NotBlank(message = "National ID is required")
        @RwandaNationalId
        @Schema(example = "1199080012345678")
        private String nationalId;

        @NotBlank(message = "Email is required")
        @ValidEmail
        private String email;

        @NotBlank(message = "Phone number is required")
        @RwandaPhone
        private String phoneNumber;

        @NotBlank(message = "Address is required")
        @Size(min = 3, max = 255, message = "Address must be between 3 and 255 characters")
        private String address;

        @NotNull(message = "Status is required")
        private EntityStatus status;

        @Positive(message = "Linked user ID must be positive")
        private Long userId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerResponse {
        private Long id;
        private String fullNames;
        private String nationalId;
        private String email;
        private String phoneNumber;
        private String address;
        private EntityStatus status;
        private Long userId;
        private LocalDateTime createdAt;
    }
}
