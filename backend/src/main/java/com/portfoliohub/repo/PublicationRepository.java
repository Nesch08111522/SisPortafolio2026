package com.portfoliohub.repo;

import com.portfoliohub.domain.Publication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PublicationRepository extends JpaRepository<Publication, UUID> {
    List<Publication> findByUserIdOrderByPublishedOnDesc(UUID userId);
    Optional<Publication> findByIdAndUserId(UUID id, UUID userId);
}
