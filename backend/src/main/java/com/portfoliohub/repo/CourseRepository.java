package com.portfoliohub.repo;

import com.portfoliohub.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Course> findByIdAndUserId(UUID id, UUID userId);
}
