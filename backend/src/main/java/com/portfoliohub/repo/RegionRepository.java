package com.portfoliohub.repo;

import com.portfoliohub.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegionRepository extends JpaRepository<Region, UUID> {
    List<Region> findByCountryIdAndActiveTrueOrderByNameAsc(UUID countryId);
    Optional<Region> findByCountryIdAndNameIgnoreCase(UUID countryId, String name);
}
