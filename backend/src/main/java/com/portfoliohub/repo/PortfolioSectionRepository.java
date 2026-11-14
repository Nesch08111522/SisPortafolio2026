package com.portfoliohub.repo;

import com.portfoliohub.domain.PortfolioSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioSectionRepository extends JpaRepository<PortfolioSection, UUID> {
    List<PortfolioSection> findByPortfolioIdOrderBySortOrderAsc(UUID portfolioId);
    Optional<PortfolioSection> findByPortfolioIdAndSectionKey(UUID portfolioId, String sectionKey);
    void deleteByPortfolioIdAndSectionKey(UUID portfolioId, String sectionKey);
}
