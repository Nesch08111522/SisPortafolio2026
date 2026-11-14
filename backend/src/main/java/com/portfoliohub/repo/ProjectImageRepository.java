package com.portfoliohub.repo;

import com.portfoliohub.domain.ProjectImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectImageRepository extends JpaRepository<ProjectImage, UUID> {
    List<ProjectImage> findByProjectIdOrderBySortOrderAsc(UUID projectId);
    void deleteByProjectId(UUID projectId);
}
