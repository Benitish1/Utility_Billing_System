package com.wasac.billing.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response models for exposing audit history to administrators.
 */
public final class AuditLogDtos {
    private AuditLogDtos() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditLogResponse {
        private Long id;
        private String actorEmail;
        private String actorRole;
        private String action;
        private String entityType;
        private Long entityId;
        private String description;
        private LocalDateTime createdAt;
    }
}
