package com.wasac.billing.service;

import com.wasac.billing.dto.TariffDtos;

import java.util.List;

public interface TariffService {
    TariffDtos.TariffResponse create(TariffDtos.TariffRequest request);
    TariffDtos.TariffResponse getById(Long id);
    List<TariffDtos.TariffResponse> getAll();
}
