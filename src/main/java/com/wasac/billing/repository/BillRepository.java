package com.wasac.billing.repository;

import com.wasac.billing.entity.Bill;
import com.wasac.billing.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByBillNumber(String billNumber);

    List<Bill> findByCustomerId(Long customerId);

    List<Bill> findByStatus(BillStatus status);

    long countByBillingYearAndBillingMonth(int billingYear, int billingMonth);

    boolean existsByReadingId(Long readingId);
}
