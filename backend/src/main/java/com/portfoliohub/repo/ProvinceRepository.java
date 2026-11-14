package com.portfoliohub.repo;

import com.portfoliohub.domain.Province;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProvinceRepository extends JpaRepository<Province, UUID> {
    List<Province> findByRegionIdAndActiveTrueOrderByNameAsc(UUID regionId);
}
