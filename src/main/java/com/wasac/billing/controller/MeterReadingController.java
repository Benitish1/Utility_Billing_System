package com.wasac.billing.controller;

import com.wasac.billing.dto.ApiResponse;
import com.wasac.billing.dto.ReadingDtos;
import com.wasac.billing.service.MeterReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
@Tag(name = "Meter Reading", description = "Meter reading capture endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class MeterReadingController {

    private final MeterReadingService meterReadingService;

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    @Operation(summary = "Capture meter reading", description = "Access: ROLE_OPERATOR only. Meter must be active. One reading per meter/month/year.")
    public ResponseEntity<ApiResponse<ReadingDtos.ReadingResponse>> create(@Valid @RequestBody ReadingDtos.ReadingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Meter reading captured", meterReadingService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR')")
    @Operation(summary = "Get reading by ID", description = "Access: ROLE_ADMIN, ROLE_FINANCE, ROLE_OPERATOR")
    public ResponseEntity<ApiResponse<ReadingDtos.ReadingResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Reading retrieved", meterReadingService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR')")
    @Operation(summary = "List all readings", description = "Access: ROLE_ADMIN, ROLE_FINANCE, ROLE_OPERATOR")
    public ResponseEntity<ApiResponse<List<ReadingDtos.ReadingResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Readings retrieved", meterReadingService.getAll()));
    }
}
