package com.portfoliohub.repo;

import com.portfoliohub.domain.DocumentExtraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentExtractionRepository extends JpaRepository<DocumentExtraction, UUID> {
    Optional<DocumentExtraction> findByIdAndDocumentId(UUID id, UUID documentId);
    List<DocumentExtraction> findByDocumentIdOrderByCreatedAtDesc(UUID documentId);
}
