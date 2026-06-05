package com.wasac.billing.repository;

import com.wasac.billing.entity.Tariff;
import com.wasac.billing.enums.MeterType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TariffRepository extends JpaRepository<Tariff, Long> {

    List<Tariff> findByMeterType(MeterType meterType);

    Optional<Tariff> findFirstByMeterTypeAndActiveTrueOrderByVersionDesc(MeterType meterType);

    Optional<Tariff> findFirstByMeterTypeAndEffectiveFromLessThanEqualAndActiveTrueOrderByVersionDesc(
            MeterType meterType, LocalDate effectiveDate);

    boolean existsByMeterTypeAndVersion(MeterType meterType, Integer version);
}
