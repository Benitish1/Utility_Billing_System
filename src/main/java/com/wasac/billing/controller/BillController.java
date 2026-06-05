package com.wasac.billing.controller;

import com.wasac.billing.dto.ApiResponse;
import com.wasac.billing.dto.BillingDtos;
import com.wasac.billing.service.BillingService;
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
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Tag(name = "Billing", description = "Bill generation and retrieval endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class BillController {

    private final BillingService billingService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Generate bill from reading", description = "Access: ROLE_ADMIN, ROLE_FINANCE. Auto-calculates VAT and creates notification.")
    public ResponseEntity<ApiResponse<BillingDtos.BillResponse>> generate(@Valid @RequestBody BillingDtos.GenerateBillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bill generated successfully", billingService.generateBill(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "Get bill by ID", description = "Access: ROLE_ADMIN, ROLE_FINANCE (all bills), ROLE_CUSTOMER (own bills only)")
    public ResponseEntity<ApiResponse<BillingDtos.BillResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Bill retrieved", billingService.getById(id)));
    }

    @GetMapping("/number/{billNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "Get bill by number", description = "Access: ROLE_ADMIN, ROLE_FINANCE, ROLE_CUSTOMER (own bills only)")
    public ResponseEntity<ApiResponse<BillingDtos.BillResponse>> getByNumber(@PathVariable String billNumber) {
        return ResponseEntity.ok(ApiResponse.success("Bill retrieved", billingService.getByBillNumber(billNumber)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "List all bills", description = "Access: ROLE_ADMIN, ROLE_FINANCE")
    public ResponseEntity<ApiResponse<List<BillingDtos.BillResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Bills retrieved", billingService.getAll()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get my bills", description = "Access: ROLE_CUSTOMER only")
    public ResponseEntity<ApiResponse<List<BillingDtos.BillResponse>>> getMyBills() {
        return ResponseEntity.ok(ApiResponse.success("Your bills retrieved", billingService.getMyBills()));
    }
}
