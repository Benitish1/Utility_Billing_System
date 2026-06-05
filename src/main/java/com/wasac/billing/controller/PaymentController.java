package com.wasac.billing.controller;

import com.wasac.billing.dto.ApiResponse;
import com.wasac.billing.dto.PaymentDtos;
import com.wasac.billing.service.PaymentService;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "Record payment", description = "Access: ROLE_CUSTOMER for own bills, ROLE_ADMIN and ROLE_FINANCE for any bill. Supports partial/full payments and prevents overpayment.")
    public ResponseEntity<ApiResponse<PaymentDtos.PaymentResponse>> record(@Valid @RequestBody PaymentDtos.PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded", paymentService.recordPayment(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "List all payments", description = "Access: ROLE_ADMIN, ROLE_FINANCE")
    public ResponseEntity<ApiResponse<List<PaymentDtos.PaymentResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved", paymentService.getAll()));
    }

    @GetMapping("/bill/{billId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Get payments by bill", description = "Access: ROLE_ADMIN, ROLE_FINANCE")
    public ResponseEntity<ApiResponse<List<PaymentDtos.PaymentResponse>>> getByBill(@PathVariable Long billId) {
        return ResponseEntity.ok(ApiResponse.success("Bill payments retrieved", paymentService.getByBill(billId)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get my payments", description = "Access: ROLE_CUSTOMER only")
    public ResponseEntity<ApiResponse<List<PaymentDtos.PaymentResponse>>> getMyPayments() {
        return ResponseEntity.ok(ApiResponse.success("Your payments retrieved", paymentService.getMyPayments()));
    }
}
