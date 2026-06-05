package com.wasac.billing.service;

import com.wasac.billing.dto.BillingDtos;

import java.util.List;

public interface BillingService {
    BillingDtos.BillResponse generateBill(BillingDtos.GenerateBillRequest request);
    BillingDtos.BillResponse getById(Long id);
    BillingDtos.BillResponse getByBillNumber(String billNumber);
    List<BillingDtos.BillResponse> getAll();
    List<BillingDtos.BillResponse> getMyBills();
}
