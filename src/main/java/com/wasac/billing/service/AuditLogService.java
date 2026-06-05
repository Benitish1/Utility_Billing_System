package com.wasac.billing.service;

import com.wasac.billing.dto.AuditLogDtos;

import java.util.List;

public interface AuditLogService {
    void log(String action, String entityType, Long entityId, String description);
    List<AuditLogDtos.AuditLogResponse> getRecent();
    List<AuditLogDtos.AuditLogResponse> getByActor(String actorEmail);
    List<AuditLogDtos.AuditLogResponse> getByEntity(String entityType, Long entityId);
}
