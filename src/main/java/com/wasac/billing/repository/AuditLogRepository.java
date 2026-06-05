package com.wasac.billing.repository;

import com.wasac.billing.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByActorEmailOrderByCreatedAtDesc(String actorEmail);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    List<AuditLog> findTop100ByOrderByCreatedAtDesc();
}
