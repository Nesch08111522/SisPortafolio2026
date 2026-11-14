package com.portfoliohub.repo;

import com.portfoliohub.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CountryRepository extends JpaRepository<Country, UUID> {
    List<Country> findByActiveTrueOrderByNameAsc();
    Optional<Country> findByIso2IgnoreCase(String iso2);
}
