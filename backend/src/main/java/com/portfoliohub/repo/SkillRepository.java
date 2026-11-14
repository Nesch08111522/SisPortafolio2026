package com.portfoliohub.repo;

import com.portfoliohub.domain.Skill;
import com.portfoliohub.domain.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {
    Optional<Skill> findByNameIgnoreCaseAndCategory(String name, SkillCategory category);
}
