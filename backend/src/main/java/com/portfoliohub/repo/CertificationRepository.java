package com.portfoliohub.repo;

import com.portfoliohub.domain.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CertificationRepository extends JpaRepository<Certification, UUID> {
    List<Certification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Certification> findByIdAndUserId(UUID id, UUID userId);
}
