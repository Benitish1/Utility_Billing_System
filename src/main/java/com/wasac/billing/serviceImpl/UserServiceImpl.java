package com.wasac.billing.serviceImpl;

import com.wasac.billing.dto.AuthDtos;
import com.wasac.billing.entity.User;
import com.wasac.billing.enums.UserStatus;
import com.wasac.billing.exception.BusinessException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.UserRepository;
import com.wasac.billing.service.AuditLogService;
import com.wasac.billing.service.UserService;
import com.wasac.billing.utils.RwandaPhoneValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public AuthDtos.UserResponse createUser(AuthDtos.SignupRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String phone = RwandaPhoneValidator.normalize(request.getPhoneNumber());

        if (!request.getEmail().equals(email)) {
            throw new BusinessException("Email must be lowercase only");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email already exists", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByPhoneNumber(phone)) {
            throw new BusinessException("Phone number already exists", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .phoneNumber(phone)
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .role(request.getRole())
                .emailVerified(true)
                .build();
        User saved = userRepository.save(user);
        auditLogService.log("CREATE_USER", "User", saved.getId(),
                "Created user " + saved.getEmail() + " with role " + saved.getRole());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthDtos.UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthDtos.UserResponse getById(Long id) {
        return toResponse(findUser(id));
    }

    @Override
    @Transactional
    public AuthDtos.UserResponse updateStatus(Long id, String status) {
        User user = findUser(id);
        try {
            user.setStatus(UserStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Invalid status. Use ACTIVE or INACTIVE");
        }
        User saved = userRepository.save(user);
        auditLogService.log("UPDATE_USER_STATUS", "User", saved.getId(),
                "Updated user " + saved.getEmail() + " status to " + saved.getStatus());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        User user = findUser(id);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        auditLogService.log("DEACTIVATE_USER", "User", user.getId(),
                "Deactivated user " + user.getEmail());
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    private AuthDtos.UserResponse toResponse(User user) {
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
