package com.wasac.billing.service;

import com.wasac.billing.dto.NotificationDtos;

import java.util.List;

public interface NotificationService {
    List<NotificationDtos.NotificationResponse> getMyNotifications();
    List<NotificationDtos.NotificationResponse> getByCustomer(Long customerId);
}
