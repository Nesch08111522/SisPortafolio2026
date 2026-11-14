package com.portfoliohub.repo;

import com.portfoliohub.domain.PortfolioVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioVersionRepository extends JpaRepository<PortfolioVersion, UUID> {
    List<PortfolioVersion> findTop30ByPortfolioIdOrderByCreatedAtDesc(UUID portfolioId);
    Optional<PortfolioVersion> findFirstByPortfolioIdOrderByCreatedAtAsc(UUID portfolioId);
    long countByPortfolioId(UUID portfolioId);
}
