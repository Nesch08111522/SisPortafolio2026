package com.portfoliohub.repo;

import com.portfoliohub.domain.TemplateDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateDefinitionRepository extends JpaRepository<TemplateDefinition, UUID> {
    List<TemplateDefinition> findByActiveTrueOrderBySortOrderAsc();
    Optional<TemplateDefinition> findByCode(String code);
}
