package com.portfoliohub.repo;

import com.portfoliohub.domain.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserSkillRepository extends JpaRepository<UserSkill, UserSkill.Key> {
    List<UserSkill> findByUserId(UUID userId);
    void deleteByUserIdAndSkillId(UUID userId, UUID skillId);
}
