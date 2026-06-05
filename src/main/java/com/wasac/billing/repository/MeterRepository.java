package com.wasac.billing.repository;

import com.wasac.billing.entity.Meter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeterRepository extends JpaRepository<Meter, Long> {

    Optional<Meter> findByMeterNumber(String meterNumber);

    List<Meter> findByCustomerId(Long customerId);

    boolean existsByMeterNumber(String meterNumber);
}
