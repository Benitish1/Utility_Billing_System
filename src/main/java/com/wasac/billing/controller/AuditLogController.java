package com.wasac.billing.controller;

import com.wasac.billing.dto.ApiResponse;
import com.wasac.billing.dto.AuditLogDtos;
import com.wasac.billing.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Admin-only endpoints for reviewing important system actions")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List recent audit logs", description = "Access: ROLE_ADMIN only. Returns latest 100 audit records.")
    public ResponseEntity<ApiResponse<List<AuditLogDtos.AuditLogResponse>>> getRecent() {
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved", auditLogService.getRecent()));
    }

    @GetMapping("/actor/{actorEmail}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List audit logs by actor", description = "Access: ROLE_ADMIN only")
    public ResponseEntity<ApiResponse<List<AuditLogDtos.AuditLogResponse>>> getByActor(@PathVariable String actorEmail) {
        return ResponseEntity.ok(ApiResponse.success("Actor audit logs retrieved", auditLogService.getByActor(actorEmail)));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List audit logs by entity", description = "Access: ROLE_ADMIN only")
    public ResponseEntity<ApiResponse<List<AuditLogDtos.AuditLogResponse>>> getByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(ApiResponse.success("Entity audit logs retrieved", auditLogService.getByEntity(entityType, entityId)));
    }
}
