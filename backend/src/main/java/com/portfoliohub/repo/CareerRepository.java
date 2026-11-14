package com.portfoliohub.repo;

import com.portfoliohub.domain.Career;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CareerRepository extends JpaRepository<Career, UUID> {
    Optional<Career> findByCode(String code);

    @Query("select distinct c from Career c left join c.faculties f where c.active = true and (:facultyId is null or f.id = :facultyId) and (:q is null or :q = '' or lower(c.name) like lower(concat('%', :q, '%')) or lower(coalesce(c.nameEn, '')) like lower(concat('%', :q, '%')) or lower(c.code) like lower(concat('%', :q, '%')))")
    Page<Career> search(UUID facultyId, String q, Pageable pageable);
}
