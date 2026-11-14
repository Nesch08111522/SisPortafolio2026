package com.portfoliohub.repo;

import com.portfoliohub.domain.GeoCacheEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GeoCacheEntryRepository extends JpaRepository<GeoCacheEntry, UUID> {
    Optional<GeoCacheEntry> findByProviderAndQueryKey(String provider, String queryKey);
}
