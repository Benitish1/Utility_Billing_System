package com.wasac.billing.service;

import com.wasac.billing.dto.CustomerDtos;

import java.util.List;

public interface CustomerService {
    CustomerDtos.CustomerResponse create(CustomerDtos.CustomerRequest request);
    CustomerDtos.CustomerResponse update(Long id, CustomerDtos.CustomerRequest request);
    CustomerDtos.CustomerResponse getById(Long id);
    List<CustomerDtos.CustomerResponse> getAll();
    void deactivate(Long id);
}
