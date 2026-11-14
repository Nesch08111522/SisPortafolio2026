package com.portfoliohub.repo;

import com.portfoliohub.domain.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpecializationRepository extends JpaRepository<Specialization, UUID> {
    List<Specialization> findByCareerIdAndActiveTrueOrderByNameAsc(UUID careerId);
    List<Specialization> findByCareerIdOrderByNameAsc(UUID careerId);
}
