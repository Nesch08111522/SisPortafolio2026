package com.portfoliohub.repo;

import com.portfoliohub.domain.PortfolioVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PortfolioVisitRepository extends JpaRepository<PortfolioVisit, UUID> {
    long countByPortfolioId(UUID portfolioId);

    @Query("select v.device, count(v) from PortfolioVisit v where v.portfolioId = :portfolioId and v.visitedAt >= :since group by v.device")
    List<Object[]> countByDevice(UUID portfolioId, Instant since);

    List<PortfolioVisit> findByPortfolioIdAndVisitedAtAfterOrderByVisitedAtDesc(UUID portfolioId, Instant since);
}
