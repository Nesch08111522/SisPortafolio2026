package com.portfoliohub.repo;

import com.portfoliohub.domain.PortfolioPublication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PortfolioPublicationRepository extends JpaRepository<PortfolioPublication, UUID> {
    List<PortfolioPublication> findByPortfolioIdOrderByPublishedAtDesc(UUID portfolioId);
}
