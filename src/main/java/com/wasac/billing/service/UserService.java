package com.wasac.billing.service;

import com.wasac.billing.dto.AuthDtos;

import java.util.List;

public interface UserService {
    AuthDtos.UserResponse createUser(AuthDtos.SignupRequest request);
    List<AuthDtos.UserResponse> getAllUsers();
    AuthDtos.UserResponse getById(Long id);
    AuthDtos.UserResponse updateStatus(Long id, String status);
    void deactivateUser(Long id);
}
