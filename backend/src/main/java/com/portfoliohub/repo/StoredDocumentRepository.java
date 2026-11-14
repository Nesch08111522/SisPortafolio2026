package com.portfoliohub.repo;

import com.portfoliohub.domain.StoredDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoredDocumentRepository extends JpaRepository<StoredDocument, UUID> {
    List<StoredDocument> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<StoredDocument> findByIdAndUserId(UUID id, UUID userId);
}
