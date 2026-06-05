package com.wasac.billing.controller;

import com.wasac.billing.dto.ApiResponse;
import com.wasac.billing.dto.MeterDtos;
import com.wasac.billing.service.MeterService;
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
@RequestMapping("/api/meters")
@RequiredArgsConstructor
@Tag(name = "Meter Management", description = "Utility meter management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class MeterController {

    private final MeterService meterService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Create meter", description = "Access: ROLE_ADMIN, ROLE_FINANCE")
    public ResponseEntity<ApiResponse<MeterDtos.MeterResponse>> create(@Valid @RequestBody MeterDtos.MeterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Meter created", meterService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Update meter", description = "Access: ROLE_ADMIN, ROLE_FINANCE")
    public ResponseEntity<ApiResponse<MeterDtos.MeterResponse>> update(
            @PathVariable Long id, @Valid @RequestBody MeterDtos.MeterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Meter updated", meterService.update(id, request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR')")
    @Operation(summary = "Get meter by ID", description = "Access: ROLE_ADMIN, ROLE_FINANCE, ROLE_OPERATOR")
    public ResponseEntity<ApiResponse<MeterDtos.MeterResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Meter retrieved", meterService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR')")
    @Operation(summary = "List all meters", description = "Access: ROLE_ADMIN, ROLE_FINANCE, ROLE_OPERATOR")
    public ResponseEntity<ApiResponse<List<MeterDtos.MeterResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Meters retrieved", meterService.getAll()));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR')")
    @Operation(summary = "Get meters by customer", description = "Access: ROLE_ADMIN, ROLE_FINANCE, ROLE_OPERATOR")
    public ResponseEntity<ApiResponse<List<MeterDtos.MeterResponse>>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success("Customer meters retrieved", meterService.getByCustomer(customerId)));
    }
}
