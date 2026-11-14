package com.portfoliohub.service;

import com.portfoliohub.domain.Career;
import com.portfoliohub.domain.CareerTemplateConfig;
import com.portfoliohub.domain.TemplateDefinition;
import com.portfoliohub.repo.CareerRepository;
import com.portfoliohub.repo.CareerSynonymRepository;
import com.portfoliohub.repo.CareerTemplateConfigRepository;
import com.portfoliohub.repo.CityRepository;
import com.portfoliohub.repo.CountryRepository;
import com.portfoliohub.repo.DistrictRepository;
import com.portfoliohub.repo.FacultyRepository;
import com.portfoliohub.repo.ProvinceRepository;
import com.portfoliohub.repo.RegionRepository;
import com.portfoliohub.repo.SpecializationRepository;
import com.portfoliohub.repo.TemplateDefinitionRepository;
import com.portfoliohub.repo.UniversityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final FacultyRepository faculties;
    private final CareerRepository careers;
    private final CareerSynonymRepository synonyms;
    private final SpecializationRepository specializations;
    private final UniversityRepository universities;
    private final CountryRepository countries;
    private final RegionRepository regions;
    private final ProvinceRepository provinces;
    private final CityRepository cities;
    private final DistrictRepository districts;
    private final TemplateDefinitionRepository templates;
    private final CareerTemplateConfigRepository configs;

    public CatalogService(
            FacultyRepository faculties,
            CareerRepository careers,
            CareerSynonymRepository synonyms,
            SpecializationRepository specializations,
            UniversityRepository universities,
            CountryRepository countries,
            RegionRepository regions,
            ProvinceRepository provinces,
            CityRepository cities,
            DistrictRepository districts,
            TemplateDefinitionRepository templates,
            CareerTemplateConfigRepository configs
    ) {
        this.faculties = faculties;
        this.careers = careers;
        this.synonyms = synonyms;
        this.specializations = specializations;
        this.universities = universities;
        this.countries = countries;
        this.regions = regions;
        this.provinces = provinces;
        this.cities = cities;
        this.districts = districts;
        this.templates = templates;
        this.configs = configs;
    }

    public List<Map<String, Object>> faculties() {
        return faculties.findByActiveTrueOrderByNameAsc().stream().map(faculty -> Map.<String, Object>of(
                "id", faculty.getId(),
                "code", faculty.getCode(),
                "name", faculty.getName(),
                "nameEn", faculty.getNameEn() == null ? "" : faculty.getNameEn(),
                "description", faculty.getDescription() == null ? "" : faculty.getDescription()
        )).toList();
    }

    public Page<Map<String, Object>> careers(UUID facultyId, String q, int page, int size) {
        return careers.search(facultyId, q == null ? "" : q.trim(), PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)))
                .map(this::careerCard);
    }

    private Map<String, Object> careerCard(Career career) {
        return Map.of(
                "id", career.getId(),
                "code", career.getCode(),
                "name", career.getName(),
                "nameEn", career.getNameEn() == null ? "" : career.getNameEn(),
                "iscedCode", career.getIscedCode() == null ? "" : career.getIscedCode()
        );
    }

    public Map<String, Object> career(UUID id) {
        Career career = careers.findById(id).orElseThrow(() -> com.portfoliohub.common.ApiException.notFound("No encontramos esa carrera."));
        return Map.of(
                "id", career.getId(),
                "code", career.getCode(),
                "name", career.getName(),
                "nameEn", career.getNameEn() == null ? "" : career.getNameEn(),
                "iscedCode", career.getIscedCode() == null ? "" : career.getIscedCode(),
                "description", career.getDescription() == null ? "" : career.getDescription(),
                "faculties", career.getFaculties().stream().map(faculty -> Map.of("id", faculty.getId(), "name", faculty.getName(), "code", faculty.getCode())).toList(),
                "synonyms", synonyms.findByCareerId(career.getId()).stream().map(item -> Map.of("synonym", item.getSynonym(), "locale", item.getLocale())).toList(),
                "specializations", specializations.findByCareerIdAndActiveTrueOrderByNameAsc(career.getId()).stream().map(item -> Map.of("id", item.getId(), "name", item.getName())).toList()
        );
    }

    public List<Map<String, Object>> specializations(UUID careerId) {
        return specializations.findByCareerIdAndActiveTrueOrderByNameAsc(careerId).stream()
                .map(item -> Map.<String, Object>of("id", item.getId(), "name", item.getName()))
                .toList();
    }

    public Page<com.portfoliohub.domain.University> universities(String q, int page, int size) {
        PageRequest request = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        if (q == null || q.isBlank()) {
            return universities.findByActiveTrue(request);
        }
        return universities.findByActiveTrueAndNameContainingIgnoreCase(q.trim(), request);
    }

    public List<com.portfoliohub.domain.Country> countries() {
        return countries.findByActiveTrueOrderByNameAsc();
    }

    public List<com.portfoliohub.domain.Region> regions(UUID countryId) {
        return regions.findByCountryIdAndActiveTrueOrderByNameAsc(countryId);
    }

    public List<com.portfoliohub.domain.Province> provinces(UUID regionId) {
        return provinces.findByRegionIdAndActiveTrueOrderByNameAsc(regionId);
    }

    public List<com.portfoliohub.domain.City> cities(UUID countryId, UUID regionId, UUID provinceId, String q) {
        if (countryId == null) {
            return List.of();
        }
        return cities.search(countryId, regionId, provinceId, q == null || q.isBlank() ? null : q.trim());
    }

    public List<com.portfoliohub.domain.District> districts(UUID cityId) {
        return districts.findByCityIdAndActiveTrueOrderByNameAsc(cityId);
    }

    public List<TemplateDefinition> templates() {
        return templates.findByActiveTrueOrderBySortOrderAsc();
    }

    public List<Map<String, Object>> recommended(UUID careerId) {
        List<CareerTemplateConfig> rows = careerId == null
                ? configs.findByCareerIdIsNullAndActiveTrueOrderBySortOrderAsc()
                : configs.findByCareerIdAndActiveTrueOrderBySortOrderAsc(careerId);
        if (rows.isEmpty()) {
            rows = configs.findByCareerIdIsNullAndActiveTrueOrderBySortOrderAsc();
        }
        Map<UUID, TemplateDefinition> byId = new LinkedHashMap<>();
        templates.findAll().forEach(template -> byId.put(template.getId(), template));
        return rows.stream().map(config -> {
            TemplateDefinition template = byId.get(config.getTemplateId());
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("configId", config.getId());
            view.put("templateId", config.getTemplateId());
            view.put("code", template == null ? "" : template.getCode());
            view.put("name", template == null ? config.getVariantName() : template.getName());
            view.put("description", template == null ? "" : template.getDescription());
            view.put("layout", template == null ? "" : template.getLayout());
            view.put("variantName", config.getVariantName());
            view.put("sections", config.getSectionSchema());
            view.put("competencies", config.getCompetencies());
            view.put("examples", config.getContentExamples());
            view.put("disclaimer", config.getDisclaimer());
            view.put("studentSafe", config.isStudentSafe());
            view.put("theme", template == null ? null : template.getThemeDefaults());
            return view;
        }).toList();
    }
}
