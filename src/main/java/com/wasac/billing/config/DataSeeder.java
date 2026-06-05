package com.wasac.billing.config;

import com.wasac.billing.entity.Customer;
import com.wasac.billing.entity.Meter;
import com.wasac.billing.entity.Tariff;
import com.wasac.billing.entity.User;
import com.wasac.billing.enums.*;
import com.wasac.billing.repository.CustomerRepository;
import com.wasac.billing.repository.MeterRepository;
import com.wasac.billing.repository.TariffRepository;
import com.wasac.billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final MeterRepository meterRepository;
    private final TariffRepository tariffRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedTariffs();
        seedCustomer();
        seedMeter();
    }

    private void seedUsers() {
        createUserIfMissing("System Admin", "admin@wasac.rw", "+250788000001", Role.ROLE_ADMIN);
        createUserIfMissing("Meter Operator", "operator@wasac.rw", "+250788000002", Role.ROLE_OPERATOR);
        createUserIfMissing("Finance Officer", "finance@wasac.rw", "+250788000003", Role.ROLE_FINANCE);
        createUserIfMissing("Jean Customer", "customer@wasac.rw", "+250788000004", Role.ROLE_CUSTOMER);
    }

    private void createUserIfMissing(String name, String email, String phone, Role role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = User.builder()
                .fullName(name)
                .email(email)
                .phoneNumber(phone)
                .password(passwordEncoder.encode("Admin@123"))
                .status(UserStatus.ACTIVE)
                .role(role)
                .emailVerified(true)
                .build();
        userRepository.save(user);
        log.info("Seeded user: {}", email);
    }

    private void seedTariffs() {
        if (tariffRepository.count() > 0) {
            return;
        }
        tariffRepository.save(Tariff.builder()
                .meterType(MeterType.WATER)
                .tariffType(TariffType.FLAT)
                .pricePerUnit(new BigDecimal("350.00"))
                .fixedCharge(new BigDecimal("2000.00"))
                .vatPercentage(new BigDecimal("18.00"))
                .penaltyPercentage(new BigDecimal("5.00"))
                .version(1)
                .effectiveFrom(LocalDate.of(2025, 1, 1))
                .active(true)
                .build());
        tariffRepository.save(Tariff.builder()
                .meterType(MeterType.ELECTRICITY)
                .tariffType(TariffType.FLAT)
                .pricePerUnit(new BigDecimal("180.00"))
                .fixedCharge(new BigDecimal("1500.00"))
                .vatPercentage(new BigDecimal("18.00"))
                .penaltyPercentage(new BigDecimal("5.00"))
                .version(1)
                .effectiveFrom(LocalDate.of(2025, 1, 1))
                .active(true)
                .build());
        log.info("Seeded default tariffs");
    }

    private void seedCustomer() {
        if (customerRepository.existsByEmail("customer@wasac.rw")) {
            return;
        }
        User customerUser = userRepository.findByEmail("customer@wasac.rw").orElse(null);
        Customer customer = Customer.builder()
                .fullNames("Jean Baptiste Uwimana")
                .nationalId("1199080012345678")
                .email("customer@wasac.rw")
                .phoneNumber("+250788000004")
                .address("KG 15 Ave, Kigali, Rwanda")
                .status(EntityStatus.ACTIVE)
                .user(customerUser)
                .build();
        customerRepository.save(customer);
        log.info("Seeded sample customer");
    }

    private void seedMeter() {
        customerRepository.findByEmail("customer@wasac.rw").ifPresent(customer -> {
            if (!meterRepository.existsByMeterNumber("WTR-SEED-001")) {
                meterRepository.save(Meter.builder()
                        .meterNumber("WTR-SEED-001")
                        .meterType(MeterType.WATER)
                        .installationDate(LocalDate.of(2025, 1, 15))
                        .status(EntityStatus.ACTIVE)
                        .customer(customer)
                        .build());
            }
            if (!meterRepository.existsByMeterNumber("ELC-SEED-001")) {
                meterRepository.save(Meter.builder()
                        .meterNumber("ELC-SEED-001")
                        .meterType(MeterType.ELECTRICITY)
                        .installationDate(LocalDate.of(2025, 1, 15))
                        .status(EntityStatus.ACTIVE)
                        .customer(customer)
                        .build());
            }
            log.info("Seeded sample meters");
        });
    }
}
