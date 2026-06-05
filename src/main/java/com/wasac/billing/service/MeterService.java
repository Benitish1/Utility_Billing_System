package com.wasac.billing.service;

import com.wasac.billing.dto.MeterDtos;

import java.util.List;

public interface MeterService {
    MeterDtos.MeterResponse create(MeterDtos.MeterRequest request);
    MeterDtos.MeterResponse update(Long id, MeterDtos.MeterRequest request);
    MeterDtos.MeterResponse getById(Long id);
    List<MeterDtos.MeterResponse> getAll();
    List<MeterDtos.MeterResponse> getByCustomer(Long customerId);
}
