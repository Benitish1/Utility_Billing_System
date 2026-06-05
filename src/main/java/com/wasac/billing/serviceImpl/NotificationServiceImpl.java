package com.wasac.billing.serviceImpl;

import com.wasac.billing.dto.NotificationDtos;
import com.wasac.billing.entity.Customer;
import com.wasac.billing.entity.User;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.CustomerRepository;
import com.wasac.billing.repository.NotificationRepository;
import com.wasac.billing.service.NotificationService;
import com.wasac.billing.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDtos.NotificationResponse> getMyNotifications() {
        User user = SecurityUtils.getCurrentUser();
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No customer profile linked to your account"));
        return getByCustomer(customer.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDtos.NotificationResponse> getByCustomer(Long customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + customerId));
        return notificationRepository.findByCustomerIdOrderBySentAtDesc(customerId)
                .stream()
                .map(n -> NotificationDtos.NotificationResponse.builder()
                        .id(n.getId())
                        .customerId(n.getCustomer().getId())
                        .customerName(n.getCustomer().getFullNames())
                        .message(n.getMessage())
                        .sentAt(n.getSentAt())
                        .status(n.getStatus())
                        .build())
                .toList();
    }
}
