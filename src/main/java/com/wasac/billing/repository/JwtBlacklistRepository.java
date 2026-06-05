package com.wasac.billing.repository;

import com.wasac.billing.entity.JwtBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface JwtBlacklistRepository extends JpaRepository<JwtBlacklist, Long> {

    boolean existsByToken(String token);

    void deleteByExpiryBefore(LocalDateTime expiry);
}
