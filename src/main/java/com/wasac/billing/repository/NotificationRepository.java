package com.wasac.billing.repository;

import com.wasac.billing.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCustomerIdOrderBySentAtDesc(Long customerId);
}
