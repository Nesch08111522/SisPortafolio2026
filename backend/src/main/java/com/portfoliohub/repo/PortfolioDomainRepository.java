package com.portfoliohub.repo;

import com.portfoliohub.domain.PortfolioDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PortfolioDomainRepository extends JpaRepository<PortfolioDomain, UUID> {
    List<PortfolioDomain> findByPortfolioIdOrderByCreatedAtDesc(UUID portfolioId);
}
