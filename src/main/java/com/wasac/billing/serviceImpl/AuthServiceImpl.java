package com.wasac.billing.serviceImpl;

import com.wasac.billing.dto.AuthDtos;
import com.wasac.billing.entity.JwtBlacklist;
import com.wasac.billing.entity.User;
import com.wasac.billing.enums.Role;
import com.wasac.billing.enums.UserStatus;
import com.wasac.billing.exception.BusinessException;
import com.wasac.billing.exception.UnauthorizedException;
import com.wasac.billing.repository.JwtBlacklistRepository;
import com.wasac.billing.repository.UserRepository;
import com.wasac.billing.security.CustomUserDetails;
import com.wasac.billing.security.JwtTokenProvider;
import com.wasac.billing.service.AuthService;
import com.wasac.billing.service.EmailService;
import com.wasac.billing.utils.AppUtils;
import com.wasac.billing.utils.RwandaPhoneValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtBlacklistRepository jwtBlacklistRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Value("${otp.expiration-minutes:10}")
    private long otpExpirationMinutes;

    @Override
    @Transactional
    public AuthDtos.UserResponse signup(AuthDtos.SignupRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String phone = RwandaPhoneValidator.normalize(request.getPhoneNumber());
        if (request.getRole() != Role.ROLE_CUSTOMER) {
            throw new BusinessException("Public signup is allowed for customers only. Staff and admin accounts must be created by an admin");
        }
        if (!request.getEmail().equals(email)) {
            throw new BusinessException("Email must be lowercase only");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email already exists", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByPhoneNumber(phone)) {
            throw new BusinessException("Phone number already exists", HttpStatus.CONFLICT);
        }

        String otp = AppUtils.generateOtp();
        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .phoneNumber(phone)
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .role(request.getRole())
                .otpCode(otp)
                .otpExpiry(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .emailVerified(false)
                .build();
        User saved = userRepository.save(user);
        emailService.sendOtpEmail(saved.getEmail(), saved.getFullName(), otp);
        return toUserResponse(saved);
    }

    @Override
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Account email is not verified. Please verify OTP before login");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Inactive users cannot login");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return AuthDtos.AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(principal))
                .refreshToken(jwtTokenProvider.generateRefreshToken(principal))
                .tokenType("Bearer")
                .user(toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void verifyOtp(AuthDtos.OtpVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BusinessException("User not found"));
        if (user.isEmailVerified()) {
            throw new BusinessException("Account is already verified");
        }
        if (AppUtils.isOtpExpired(user.getOtpExpiry())) {
            throw new BusinessException("OTP expired. Please request a new OTP");
        }
        if (!request.getOtpCode().equals(user.getOtpCode())) {
            throw new BusinessException("Invalid OTP code");
        }
        user.setEmailVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resendOtp(AuthDtos.EmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BusinessException("User not found"));
        if (user.isEmailVerified()) {
            throw new BusinessException("Account is already verified");
        }
        String otp = AppUtils.generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(otpExpirationMinutes));
        userRepository.save(user);
        emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);
    }

    @Override
    public AuthDtos.AuthResponse refreshToken(AuthDtos.RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (jwtBlacklistRepository.existsByToken(token) || !"REFRESH".equals(jwtTokenProvider.extractTokenType(token))) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        String username = jwtTokenProvider.extractUsername(token);
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UnauthorizedException("User not found"));
        CustomUserDetails details = new CustomUserDetails(user);
        if (!jwtTokenProvider.isTokenValid(token, details)) {
            throw new UnauthorizedException("Refresh token expired or invalid");
        }
        return AuthDtos.AuthResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(details))
                .refreshToken(jwtTokenProvider.generateRefreshToken(details))
                .tokenType("Bearer")
                .user(toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization Bearer token is required");
        }
        String token = bearerToken.substring(7);
        jwtBlacklistRepository.save(JwtBlacklist.builder()
                .token(token)
                .expiry(jwtTokenProvider.extractExpiration(token).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime())
                .build());
    }

    @Override
    public AuthDtos.UserResponse currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new UnauthorizedException("No authenticated user found");
        }
        return toUserResponse(details.getUser());
    }

    private AuthDtos.UserResponse toUserResponse(User user) {
        return AuthDtos.UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
