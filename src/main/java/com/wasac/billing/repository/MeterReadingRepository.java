package com.wasac.billing.repository;

import com.wasac.billing.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {

    boolean existsByMeterIdAndBillingMonthAndBillingYear(Long meterId, Integer billingMonth, Integer billingYear);

    Optional<MeterReading> findTopByMeterIdOrderByBillingYearDescBillingMonthDesc(Long meterId);
}
