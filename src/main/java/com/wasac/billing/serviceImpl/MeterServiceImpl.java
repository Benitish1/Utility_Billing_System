package com.wasac.billing.serviceImpl;

import com.wasac.billing.dto.MeterDtos;
import com.wasac.billing.entity.Customer;
import com.wasac.billing.entity.Meter;
import com.wasac.billing.exception.BusinessException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.CustomerRepository;
import com.wasac.billing.repository.MeterRepository;
import com.wasac.billing.service.AuditLogService;
import com.wasac.billing.service.MeterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterServiceImpl implements MeterService {

    private final MeterRepository meterRepository;
    private final CustomerRepository customerRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public MeterDtos.MeterResponse create(MeterDtos.MeterRequest request) {
        if (meterRepository.existsByMeterNumber(request.getMeterNumber())) {
            throw new BusinessException("Meter number already exists", HttpStatus.CONFLICT);
        }
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Meter meter = Meter.builder()
                .meterNumber(request.getMeterNumber().trim())
                .meterType(request.getMeterType())
                .installationDate(request.getInstallationDate())
                .status(request.getStatus())
                .customer(customer)
                .build();
        Meter saved = meterRepository.save(meter);
        auditLogService.log("CREATE_METER", "Meter", saved.getId(),
                "Created meter " + saved.getMeterNumber() + " for customer " + customer.getEmail());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public MeterDtos.MeterResponse update(Long id, MeterDtos.MeterRequest request) {
        Meter meter = findMeter(id);
        meterRepository.findByMeterNumber(request.getMeterNumber())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw new BusinessException("Meter number already exists", HttpStatus.CONFLICT); });
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        meter.setMeterNumber(request.getMeterNumber().trim());
        meter.setMeterType(request.getMeterType());
        meter.setInstallationDate(request.getInstallationDate());
        meter.setStatus(request.getStatus());
        meter.setCustomer(customer);
        Meter saved = meterRepository.save(meter);
        auditLogService.log("UPDATE_METER", "Meter", saved.getId(),
                "Updated meter " + saved.getMeterNumber());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MeterDtos.MeterResponse getById(Long id) {
        return toResponse(findMeter(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeterDtos.MeterResponse> getAll() {
        return meterRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeterDtos.MeterResponse> getByCustomer(Long customerId) {
        return meterRepository.findByCustomerId(customerId).stream().map(this::toResponse).toList();
    }

    private Meter findMeter(Long id) {
        return meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found with id " + id));
    }

    private MeterDtos.MeterResponse toResponse(Meter meter) {
        return MeterDtos.MeterResponse.builder()
                .id(meter.getId())
                .meterNumber(meter.getMeterNumber())
                .meterType(meter.getMeterType())
                .installationDate(meter.getInstallationDate())
                .status(meter.getStatus())
                .customerId(meter.getCustomer().getId())
                .customerName(meter.getCustomer().getFullNames())
                .build();
    }
}
