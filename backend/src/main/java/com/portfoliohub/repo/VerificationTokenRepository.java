package com.portfoliohub.repo;

import com.portfoliohub.domain.TokenType;
import com.portfoliohub.domain.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByTokenHashAndType(String tokenHash, TokenType type);
    void deleteByUserIdAndType(UUID userId, TokenType type);
}
