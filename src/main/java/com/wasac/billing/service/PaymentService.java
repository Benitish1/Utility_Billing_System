package com.wasac.billing.service;

import com.wasac.billing.dto.PaymentDtos;

import java.util.List;

public interface PaymentService {
    PaymentDtos.PaymentResponse recordPayment(PaymentDtos.PaymentRequest request);
    List<PaymentDtos.PaymentResponse> getAll();
    List<PaymentDtos.PaymentResponse> getByBill(Long billId);
    List<PaymentDtos.PaymentResponse> getMyPayments();
}
