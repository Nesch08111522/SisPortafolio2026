package com.portfoliohub.repo;

import com.portfoliohub.domain.University;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UniversityRepository extends JpaRepository<University, UUID> {
    Page<University> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
    Page<University> findByActiveTrue(Pageable pageable);
}
