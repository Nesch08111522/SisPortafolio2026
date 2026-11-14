package com.portfoliohub.repo;

import com.portfoliohub.domain.CareerSynonym;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CareerSynonymRepository extends JpaRepository<CareerSynonym, UUID> {
    List<CareerSynonym> findByCareerId(UUID careerId);
}
