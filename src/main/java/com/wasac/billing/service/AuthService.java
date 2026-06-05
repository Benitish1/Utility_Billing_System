package com.wasac.billing.service;

import com.wasac.billing.dto.AuthDtos;

public interface AuthService {
    AuthDtos.UserResponse signup(AuthDtos.SignupRequest request);
    AuthDtos.AuthResponse login(AuthDtos.LoginRequest request);
    void verifyOtp(AuthDtos.OtpVerificationRequest request);
    void resendOtp(AuthDtos.EmailRequest request);
    AuthDtos.AuthResponse refreshToken(AuthDtos.RefreshTokenRequest request);
    void logout(String bearerToken);
    AuthDtos.UserResponse currentUser();
}
