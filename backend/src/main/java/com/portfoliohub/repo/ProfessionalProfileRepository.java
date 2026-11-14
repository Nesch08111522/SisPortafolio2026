package com.portfoliohub.repo;

import com.portfoliohub.domain.ProfessionalProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfessionalProfileRepository extends JpaRepository<ProfessionalProfile, UUID> {
}
