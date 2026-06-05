package com.wasac.billing.service;

import com.wasac.billing.dto.ReadingDtos;

import java.util.List;

public interface MeterReadingService {
    ReadingDtos.ReadingResponse create(ReadingDtos.ReadingRequest request);
    ReadingDtos.ReadingResponse getById(Long id);
    List<ReadingDtos.ReadingResponse> getAll();
}
