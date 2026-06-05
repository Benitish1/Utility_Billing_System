package com.wasac.billing.serviceImpl;

import com.wasac.billing.dto.AuditLogDtos;
import com.wasac.billing.entity.AuditLog;
import com.wasac.billing.entity.User;
import com.wasac.billing.repository.AuditLogRepository;
import com.wasac.billing.service.AuditLogService;
import com.wasac.billing.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Persists audit records for important business operations.
 */
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, Long entityId, String description) {
        User actor;
        try {
            actor = SecurityUtils.getCurrentUser();
        } catch (Exception ex) {
            actor = null;
        }

        AuditLog auditLog = AuditLog.builder()
                .actorEmail(actor == null ? "SYSTEM" : actor.getEmail())
                .actorRole(actor == null ? "SYSTEM" : actor.getRole().name())
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .build();
        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDtos.AuditLogResponse> getRecent() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDtos.AuditLogResponse> getByActor(String actorEmail) {
        return auditLogRepository.findByActorEmailOrderByCreatedAtDesc(actorEmail).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDtos.AuditLogResponse> getByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogDtos.AuditLogResponse toResponse(AuditLog auditLog) {
        return AuditLogDtos.AuditLogResponse.builder()
                .id(auditLog.getId())
                .actorEmail(auditLog.getActorEmail())
                .actorRole(auditLog.getActorRole())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .description(auditLog.getDescription())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
