package com.portfoliohub.repo;

import com.portfoliohub.domain.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DistrictRepository extends JpaRepository<District, UUID> {
    List<District> findByCityIdAndActiveTrueOrderByNameAsc(UUID cityId);
}
