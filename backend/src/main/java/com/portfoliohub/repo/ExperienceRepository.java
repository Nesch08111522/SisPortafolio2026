package com.portfoliohub.repo;

import com.portfoliohub.domain.Experience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExperienceRepository extends JpaRepository<Experience, UUID> {
    List<Experience> findByUserIdOrderByStartDateDesc(UUID userId);
    Optional<Experience> findByIdAndUserId(UUID id, UUID userId);
}
