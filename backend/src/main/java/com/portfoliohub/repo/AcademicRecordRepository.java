package com.portfoliohub.repo;

import com.portfoliohub.domain.AcademicRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, UUID> {
    Optional<AcademicRecord> findFirstByUserIdAndPrimaryRecordTrueOrderByUpdatedAtDesc(UUID userId);
}
