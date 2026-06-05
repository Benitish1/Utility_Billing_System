package com.wasac.billing.repository;

import com.wasac.billing.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByNationalId(String nationalId);

    Optional<Customer> findByPhoneNumber(String phoneNumber);

    Optional<Customer> findByUserId(Long userId);

    boolean existsByEmail(String email);

    boolean existsByNationalId(String nationalId);

    boolean existsByPhoneNumber(String phoneNumber);
}
