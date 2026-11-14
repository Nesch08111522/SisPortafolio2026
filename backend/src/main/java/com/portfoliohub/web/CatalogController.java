package com.portfoliohub.web;

import com.portfoliohub.service.CatalogService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/faculties")
    public List<Map<String, Object>> faculties() {
        return catalogService.faculties();
    }

    @GetMapping("/careers")
    public Page<?> careers(
            @RequestParam(required = false) UUID facultyId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return catalogService.careers(facultyId, q, page, size);
    }

    @GetMapping("/careers/{id}")
    public Map<String, Object> career(@PathVariable UUID id) {
        return catalogService.career(id);
    }

    @GetMapping("/specializations")
    public List<Map<String, Object>> specializations(@RequestParam UUID careerId) {
        return catalogService.specializations(careerId);
    }

    @GetMapping("/universities")
    public Page<?> universities(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return catalogService.universities(q, page, size);
    }

    @GetMapping("/geography/countries")
    public List<?> countries() {
        return catalogService.countries();
    }

    @GetMapping("/geography/regions")
    public List<?> regions(@RequestParam UUID countryId) {
        return catalogService.regions(countryId);
    }

    @GetMapping("/geography/provinces")
    public List<?> provinces(@RequestParam UUID regionId) {
        return catalogService.provinces(regionId);
    }

    @GetMapping("/geography/cities")
    public List<?> cities(
            @RequestParam UUID countryId,
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) UUID provinceId,
            @RequestParam(required = false) String q
    ) {
        return catalogService.cities(countryId, regionId, provinceId, q);
    }

    @GetMapping("/geography/districts")
    public List<?> districts(@RequestParam UUID cityId) {
        return catalogService.districts(cityId);
    }

    @GetMapping("/templates")
    public List<?> templates() {
        return catalogService.templates();
    }

    @GetMapping("/templates/recommended")
    public List<Map<String, Object>> recommended(@RequestParam(required = false) UUID careerId) {
        return catalogService.recommended(careerId);
    }
}
