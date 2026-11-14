package com.portfoliohub.repo;

import com.portfoliohub.domain.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {
    List<City> findByCountryIdAndActiveTrueOrderByNameAsc(UUID countryId);
    List<City> findByRegionIdAndActiveTrueOrderByNameAsc(UUID regionId);

    @Query("select c from City c where c.active = true and c.countryId = :countryId and (:regionId is null or c.regionId = :regionId) and (:provinceId is null or c.provinceId = :provinceId) and (:q is null or lower(c.name) like lower(concat('%', :q, '%'))) order by c.name")
    List<City> search(UUID countryId, UUID regionId, UUID provinceId, String q);
}
