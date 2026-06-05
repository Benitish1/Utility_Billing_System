package com.wasac.billing.dto;

import com.wasac.billing.enums.Role;
import com.wasac.billing.enums.UserStatus;
import com.wasac.billing.utils.RwandaPhone;
import com.wasac.billing.utils.StrongPassword;
import com.wasac.billing.utils.ValidEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Authentication DTOs for signup, login, OTP verification, refresh tokens, and safe user responses.
 */
public final class AuthDtos {
    private AuthDtos() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignupRequest {
        @NotBlank(message = "Full name is required")
        @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
        @Schema(example = "Admin User")
        private String fullName;

        @NotBlank(message = "Email is required")
        @ValidEmail
        @Schema(example = "admin@wasac.rw")
        private String email;

        @NotBlank(message = "Phone number is required")
        @RwandaPhone
        @Schema(example = "+250788123456")
        private String phoneNumber;

        @NotBlank(message = "Password is required")
        @StrongPassword
        @Schema(example = "Admin@123")
        private String password;

        @NotNull(message = "Role is required")
        @Schema(example = "ROLE_CUSTOMER")
        private Role role;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @ValidEmail
        @Schema(example = "admin@wasac.rw")
        private String email;

        @NotBlank(message = "Password is required")
        @Schema(example = "Admin@123")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtpVerificationRequest {
        @NotBlank(message = "Email is required")
        @ValidEmail
        private String email;

        @NotBlank(message = "OTP code is required")
        @Size(min = 6, max = 6, message = "OTP must be 6 digits")
        @jakarta.validation.constraints.Pattern(regexp = "^\\d{6}$", message = "OTP must contain digits only")
        private String otpCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailRequest {
        @NotBlank(message = "Email is required")
        @ValidEmail
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenRequest {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private UserResponse user;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String fullName;
        private String email;
        private String phoneNumber;
        private UserStatus status;
        private Role role;
        private boolean emailVerified;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
