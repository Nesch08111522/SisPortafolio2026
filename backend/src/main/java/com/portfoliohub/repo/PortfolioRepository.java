package com.portfoliohub.repo;

import com.portfoliohub.domain.Portfolio;
import com.portfoliohub.domain.PortfolioStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {
    Optional<Portfolio> findByUserId(UUID userId);
    Optional<Portfolio> findBySlugIgnoreCase(String slug);
    boolean existsBySlugIgnoreCase(String slug);
    Page<Portfolio> findByStatusAndShowcaseOptInTrue(PortfolioStatus status, Pageable pageable);
}
