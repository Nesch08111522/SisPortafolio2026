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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
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
        return cities.search(countryId, regionId, provinceId, q == null ? "" : q.trim());
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

    public List<Map<String, String>> worldUniversities(String country, String name) {
        String query = name == null ? "" : name.trim();
        String plainCountry = Normalizer.normalize(country == null ? "" : country, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        if (query.length() < 2 || plainCountry.isBlank()) {
            return List.of();
        }
        try {
            String url = "http://universities.hipolabs.com/search?country="
                    + URLEncoder.encode(plainCountry, StandardCharsets.UTF_8)
                    + "&name=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(6)).GET().build();
            HttpResponse<String> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return List.of();
            }
            JsonNode array = new ObjectMapper().readTree(response.body());
            List<Map<String, String>> rows = new ArrayList<>();
            if (array.isArray()) {
                for (JsonNode node : array) {
                    if (rows.size() >= 12) {
                        break;
                    }
                    String university = node.path("name").asText("");
                    if (!university.isBlank()) {
                        rows.add(Map.of("name", university, "country", node.path("country").asText(plainCountry)));
                    }
                }
            }
            return rows;
        } catch (Exception exception) {
            return List.of();
        }
    }

    public Map<String, Object> worldPlaces(String country, String region) {
        String plainCountry = plain(country);
        Map<String, Object> body = new LinkedHashMap<>();
        List<Map<String, String>> regions = new ArrayList<>();
        JsonNode states = placesJson(plainCountry, country);
        if (states != null) {
            for (JsonNode state : states.path("data").path("states")) {
                String name = state.path("name").asText("").trim();
                if (!name.isBlank()) {
                    regions.add(Map.of("id", name, "name", name, "hint", state.path("state_code").asText("Región")));
                }
            }
        }
        body.put("regions", regions);
        String regionName = region == null ? "" : region.trim();
        if (regionName.isBlank()) {
            body.put("provinces", List.of());
            body.put("cities", List.of());
            body.put("districts", List.of());
            return body;
        }
        JsonNode citiesNode = readJson("https://countriesnow.space/api/v0.1/countries/state/cities/q?country="
                + encoded(plainCountry) + "&state=" + encoded(regionName));
        List<String> names = new ArrayList<>();
        if (citiesNode != null && citiesNode.path("data").isArray()) {
            for (JsonNode city : citiesNode.path("data")) {
                String name = city.asText("").trim();
                if (!name.isBlank() && names.size() < 180) {
                    names.add(name);
                }
            }
        }
        List<Map<String, String>> provinces = new ArrayList<>();
        List<Map<String, String>> cities = new ArrayList<>();
        List<Map<String, String>> districts = new ArrayList<>();
        for (String name : names) {
            String kind = placeKind(name);
            Map<String, String> row = Map.of("id", name, "name", displayPlace(name), "hint", kind);
            if ("Provincia".equals(kind)) {
                provinces.add(row);
            } else if ("Distrito".equals(kind)) {
                districts.add(row);
            } else {
                cities.add(row);
            }
        }
        if (provinces.isEmpty()) {
            provinces.addAll(named(names, "Localidad"));
        }
        if (cities.isEmpty()) {
            cities.addAll(named(names, "Ciudad"));
        }
        if (districts.isEmpty()) {
            districts.addAll(named(names, "Distrito o barrio"));
        }
        body.put("provinces", provinces);
        body.put("cities", cities);
        body.put("districts", districts);
        return body;
    }

    public List<Map<String, String>> worldCareers(String kind, String query) {
        String q = query == null ? "" : query.trim();
        if (q.length() < 2) {
            return List.of();
        }
        String wanted = kind == null ? "" : kind.trim().toLowerCase();
        List<Map<String, String>> rows = new ArrayList<>();
        if (!"technical".equals(wanted)) {
            careers.search(null, q, PageRequest.of(0, 12)).forEach(career -> rows.add(Map.of(
                    "id", career.getId().toString(),
                    "name", career.getName(),
                    "kind", "Universitaria",
                    "hint", career.getIscedCode() == null || career.getIscedCode().isBlank() ? "Universitaria" : "Universitaria · ISCED " + career.getIscedCode()
            )));
        }
        if (!"university".equals(wanted)) {
            JsonNode root = readJson("https://ec.europa.eu/esco/api/search?language=es&type=occupation&limit=12&offset=0&text=" + encoded(q));
            if (root != null) {
                for (JsonNode node : root.path("_embedded").path("results")) {
                    String title = node.path("title").asText("").trim();
                    String code = node.path("code").asText("").trim();
                    if (!title.isBlank()) {
                        rows.add(Map.of("id", "esco:" + (code.isBlank() ? title : code), "name", title, "kind", "Técnica", "hint", code.isBlank() ? "Técnica" : "Técnica · " + code));
                    }
                }
            }
        }
        return rows;
    }

    private JsonNode placesJson(String plainCountry, String country) {
        JsonNode states = readJson("https://countriesnow.space/api/v0.1/countries/states/q?country=" + encoded(plainCountry));
        if (states != null && states.path("data").path("states").size() > 0) {
            return states;
        }
        String original = country == null ? "" : country.trim();
        if (original.equals(plainCountry)) {
            return states;
        }
        return readJson("https://countriesnow.space/api/v0.1/countries/states/q?country=" + encoded(original));
    }

    private JsonNode readJson(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(8)).header("Accept", "application/json").GET().build();
            HttpResponse<String> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return null;
            }
            return new ObjectMapper().readTree(response.body());
        } catch (Exception exception) {
            return null;
        }
    }

    private List<Map<String, String>> named(List<String> names, String hint) {
        List<Map<String, String>> rows = new ArrayList<>();
        for (String name : names) {
            rows.add(Map.of("id", name, "name", displayPlace(name), "hint", hint));
        }
        return rows;
    }

    private String placeKind(String name) {
        String folded = plain(name).toLowerCase();
        if (folded.startsWith("provincia") || folded.startsWith("province") || folded.startsWith("county") || folded.startsWith("municipio") || folded.startsWith("parroquia") || folded.startsWith("departamento")) {
            return "Provincia";
        }
        if (folded.startsWith("distrito") || folded.startsWith("district") || folded.startsWith("barrio") || folded.startsWith("urb.") || folded.startsWith("urbanizacion") || folded.startsWith("asentamiento")) {
            return "Distrito";
        }
        return "Ciudad";
    }

    private String displayPlace(String name) {
        return name.replaceFirst("(?i)^(provincia de|province of|distrito de|district of)\\s+", "");
    }

    private String plain(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD).replaceAll("\\p{M}", "").trim();
    }

    private String encoded(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
