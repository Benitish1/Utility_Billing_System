package com.wasac.billing.controller;

import com.wasac.billing.dto.ApiResponse;
import com.wasac.billing.dto.AuthDtos;
import com.wasac.billing.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Signup, OTP, login, refresh, logout, and current-user endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user", description = "Public access. Sends OTP to email for verification.")
    public ResponseEntity<ApiResponse<AuthDtos.UserResponse>> signup(@Valid @RequestBody AuthDtos.SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Signup successful. Please verify OTP sent to your email", authService.signup(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Public access. Returns JWT access and refresh tokens. Unverified/inactive users cannot login.")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Public access. Activates account after registration.")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody AuthDtos.OtpVerificationRequest request) {
        authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP", description = "Public access. Resends verification OTP to email.")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody AuthDtos.EmailRequest request) {
        authService.resendOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP resent successfully", null));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh JWT token", description = "Public access. Exchange refresh token for new access token.")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> refreshToken(@Valid @RequestBody AuthDtos.RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", authService.refreshToken(request)));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Logout", description = "Authenticated access. Blacklists the current JWT token.")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authService.logout(request.getHeader("Authorization"));
        return ResponseEntity.ok(ApiResponse.success("Logout successful. Token has been invalidated", null));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get current user", description = "Authenticated access. Returns logged-in user profile.")
    public ResponseEntity<ApiResponse<AuthDtos.UserResponse>> me() {
        return ResponseEntity.ok(ApiResponse.success("Current user retrieved", authService.currentUser()));
    }
}
