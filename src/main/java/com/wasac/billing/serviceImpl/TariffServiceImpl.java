package com.wasac.billing.serviceImpl;

import com.wasac.billing.dto.TariffDtos;
import com.wasac.billing.entity.Tariff;
import com.wasac.billing.exception.BusinessException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.TariffRepository;
import com.wasac.billing.service.AuditLogService;
import com.wasac.billing.service.TariffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TariffServiceImpl implements TariffService {

    private final TariffRepository tariffRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public TariffDtos.TariffResponse create(TariffDtos.TariffRequest request) {
        if (tariffRepository.existsByMeterTypeAndVersion(request.getMeterType(), request.getVersion())) {
            throw new BusinessException("Tariff version already exists for this meter type", HttpStatus.CONFLICT);
        }

        if (Boolean.TRUE.equals(request.getActive())) {
            tariffRepository.findFirstByMeterTypeAndActiveTrueOrderByVersionDesc(request.getMeterType())
                    .ifPresent(existing -> {
                        existing.setActive(false);
                        tariffRepository.save(existing);
                    });
        }

        Tariff tariff = Tariff.builder()
                .meterType(request.getMeterType())
                .tariffType(request.getTariffType())
                .pricePerUnit(request.getPricePerUnit())
                .fixedCharge(request.getFixedCharge())
                .vatPercentage(request.getVatPercentage())
                .penaltyPercentage(request.getPenaltyPercentage())
                .version(request.getVersion())
                .effectiveFrom(request.getEffectiveFrom())
                .active(Boolean.TRUE.equals(request.getActive()))
                .build();

        Tariff saved = tariffRepository.save(tariff);
        auditLogService.log("CREATE_TARIFF", "Tariff", saved.getId(),
                "Created tariff " + saved.getMeterType() + " version " + saved.getVersion());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TariffDtos.TariffResponse getById(Long id) {
        return toResponse(findTariff(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TariffDtos.TariffResponse> getAll() {
        return tariffRepository.findAll().stream().map(this::toResponse).toList();
    }

    private Tariff findTariff(Long id) {
        return tariffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff not found with id " + id));
    }

    private TariffDtos.TariffResponse toResponse(Tariff tariff) {
        return TariffDtos.TariffResponse.builder()
                .id(tariff.getId())
                .meterType(tariff.getMeterType())
                .tariffType(tariff.getTariffType())
                .pricePerUnit(tariff.getPricePerUnit())
                .fixedCharge(tariff.getFixedCharge())
                .vatPercentage(tariff.getVatPercentage())
                .penaltyPercentage(tariff.getPenaltyPercentage())
                .version(tariff.getVersion())
                .effectiveFrom(tariff.getEffectiveFrom())
                .active(tariff.isActive())
                .build();
    }
}
