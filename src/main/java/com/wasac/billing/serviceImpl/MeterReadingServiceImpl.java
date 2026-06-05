package com.wasac.billing.serviceImpl;

import com.wasac.billing.dto.ReadingDtos;
import com.wasac.billing.entity.Meter;
import com.wasac.billing.entity.MeterReading;
import com.wasac.billing.entity.User;
import com.wasac.billing.enums.EntityStatus;
import com.wasac.billing.exception.BusinessException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.MeterReadingRepository;
import com.wasac.billing.repository.MeterRepository;
import com.wasac.billing.service.AuditLogService;
import com.wasac.billing.service.MeterReadingService;
import com.wasac.billing.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Applies operator reading rules before persisting monthly meter consumption.
 */
@Service
@RequiredArgsConstructor
public class MeterReadingServiceImpl implements MeterReadingService {

    private final MeterReadingRepository meterReadingRepository;
    private final MeterRepository meterRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ReadingDtos.ReadingResponse create(ReadingDtos.ReadingRequest request) {
        Meter meter = meterRepository.findById(request.getMeterId())
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found with id " + request.getMeterId()));

        if (meter.getStatus() != EntityStatus.ACTIVE) {
            throw new BusinessException("Meter is inactive. Readings can only be captured for active meters");
        }
        if (request.getCurrentReading().compareTo(request.getPreviousReading()) <= 0) {
            throw new BusinessException("Current reading must be greater than previous reading");
        }
        YearMonth billingPeriod = YearMonth.of(request.getBillingYear(), request.getBillingMonth());
        if (billingPeriod.isAfter(YearMonth.now())) {
            throw new BusinessException("Billing month/year cannot be in the future");
        }
        if (meterReadingRepository.existsByMeterIdAndBillingMonthAndBillingYear(
                request.getMeterId(), request.getBillingMonth(), request.getBillingYear())) {
            throw new BusinessException("A reading already exists for this meter in the specified billing month/year");
        }

        BigDecimal unitsConsumed = request.getCurrentReading().subtract(request.getPreviousReading());
        User operator = SecurityUtils.getCurrentUser();

        MeterReading reading = MeterReading.builder()
                .meter(meter)
                .previousReading(request.getPreviousReading())
                .currentReading(request.getCurrentReading())
                .unitsConsumed(unitsConsumed)
                .readingDate(request.getReadingDate())
                .billingMonth(request.getBillingMonth())
                .billingYear(request.getBillingYear())
                .createdBy(operator)
                .build();

        MeterReading saved = meterReadingRepository.save(reading);
        auditLogService.log("CAPTURE_READING", "MeterReading", saved.getId(),
                "Captured reading for meter " + meter.getMeterNumber()
                        + " for " + saved.getBillingMonth() + "/" + saved.getBillingYear());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReadingDtos.ReadingResponse getById(Long id) {
        return toResponse(findReading(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadingDtos.ReadingResponse> getAll() {
        return meterReadingRepository.findAll().stream().map(this::toResponse).toList();
    }

    private MeterReading findReading(Long id) {
        return meterReadingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter reading not found with id " + id));
    }

    private ReadingDtos.ReadingResponse toResponse(MeterReading reading) {
        return ReadingDtos.ReadingResponse.builder()
                .id(reading.getId())
                .meterId(reading.getMeter().getId())
                .meterNumber(reading.getMeter().getMeterNumber())
                .previousReading(reading.getPreviousReading())
                .currentReading(reading.getCurrentReading())
                .unitsConsumed(reading.getUnitsConsumed())
                .readingDate(reading.getReadingDate())
                .billingMonth(reading.getBillingMonth())
                .billingYear(reading.getBillingYear())
                .createdBy(reading.getCreatedBy().getId())
                .createdAt(reading.getCreatedAt())
                .build();
    }
}
