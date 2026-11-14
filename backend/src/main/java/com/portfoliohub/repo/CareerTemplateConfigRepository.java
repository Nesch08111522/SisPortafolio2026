package com.portfoliohub.repo;

import com.portfoliohub.domain.CareerTemplateConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CareerTemplateConfigRepository extends JpaRepository<CareerTemplateConfig, UUID> {
    List<CareerTemplateConfig> findByCareerIdAndActiveTrueOrderBySortOrderAsc(UUID careerId);
    List<CareerTemplateConfig> findByCareerIdIsNullAndActiveTrueOrderBySortOrderAsc();
    Optional<CareerTemplateConfig> findByCareerIdAndTemplateId(UUID careerId, UUID templateId);
    Optional<CareerTemplateConfig> findByCareerIdIsNullAndTemplateId(UUID templateId);
    boolean existsByCareerId(UUID careerId);
    boolean existsByCareerIdIsNull();
}
