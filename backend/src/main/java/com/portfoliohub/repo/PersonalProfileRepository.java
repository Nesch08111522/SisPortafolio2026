package com.portfoliohub.repo;

import com.portfoliohub.domain.PersonalProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PersonalProfileRepository extends JpaRepository<PersonalProfile, UUID> {
}
