package com.portfoliohub.repo;

import com.portfoliohub.domain.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FacultyRepository extends JpaRepository<Faculty, UUID> {
    List<Faculty> findByActiveTrueOrderByNameAsc();
    Optional<Faculty> findByCode(String code);
}
