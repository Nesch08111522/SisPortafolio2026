package com.portfoliohub.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.portfoliohub.common.ApiException;
import com.portfoliohub.common.Texts;
import com.portfoliohub.domain.Career;
import com.portfoliohub.domain.CareerTemplateConfig;
import com.portfoliohub.domain.Faculty;
import com.portfoliohub.domain.University;
import com.portfoliohub.domain.User;
import com.portfoliohub.repo.AuditLogRepository;
import com.portfoliohub.repo.CareerRepository;
import com.portfoliohub.repo.CareerTemplateConfigRepository;
import com.portfoliohub.repo.FacultyRepository;
import com.portfoliohub.repo.PortfolioRepository;
import com.portfoliohub.repo.UniversityRepository;
import com.portfoliohub.repo.UserRepository;
import com.portfoliohub.security.ClientIp;
import com.portfoliohub.service.AuditService;
import com.portfoliohub.service.PortfolioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AdminController {

    private final UserRepository users;
    private final FacultyRepository faculties;
    private final CareerRepository careers;
    private final UniversityRepository universities;
    private final CareerTemplateConfigRepository configs;
    private final AuditLogRepository auditLogs;
    private final PortfolioRepository portfolios;
    private final AuditService audit;
    private final PortfolioService portfolioService;
    private final ClientIp clientIp;

    public AdminController(
            UserRepository users,
            FacultyRepository faculties,
            CareerRepository careers,
            UniversityRepository universities,
            CareerTemplateConfigRepository configs,
            AuditLogRepository auditLogs,
            PortfolioRepository portfolios,
            AuditService audit,
            PortfolioService portfolioService,
            ClientIp clientIp
    ) {
        this.users = users;
        this.faculties = faculties;
        this.careers = careers;
        this.universities = universities;
        this.configs = configs;
        this.auditLogs = auditLogs;
        this.portfolios = portfolios;
        this.audit = audit;
        this.portfolioService = portfolioService;
        this.clientIp = clientIp;
    }

    @GetMapping("/admin/users")
    public Object users(@RequestParam(defaultValue = "0") int page) {
        return users.findAllByOrderByCreatedAtDesc(PageRequest.of(page, 20)).map(user -> Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "enabled", user.isEnabled(),
                "emailVerified", user.isEmailVerified(),
                "roles", user.getRoles().stream().map(role -> role.getName()).toList()
        ));
    }

    @PatchMapping("/admin/users/{id}")
    @Transactional
    public Map<String, Object> updateUser(@PathVariable UUID id, @RequestBody Map<String, Boolean> body, HttpServletRequest request) {
        User user = users.findById(id).orElseThrow(() -> ApiException.notFound("No encontramos a esa persona."));
        if (body.containsKey("enabled")) {
            user.setEnabled(Boolean.TRUE.equals(body.get("enabled")));
        }
        audit.record(com.portfoliohub.security.CurrentUser.id(), "ADMIN_USER_UPDATE", "USER", id.toString(), String.valueOf(body), clientIp.resolve(request));
        return Map.of("id", user.getId(), "enabled", user.isEnabled());
    }

    @GetMapping("/admin/faculties")
    public Object faculties() {
        return faculties.findAll();
    }

    @PostMapping("/admin/faculties")
    public Faculty createFaculty(@RequestBody FacultyBody body) {
        Faculty faculty = new Faculty();
        faculty.setCode(Texts.limit(body.code(), 40));
        faculty.setName(Texts.limit(body.name(), 180));
        faculty.setNameEn(Texts.limit(body.nameEn(), 180));
        faculty.setDescription(Texts.limit(body.description(), 1000));
        if (faculty.getCode() == null || faculty.getName() == null) {
            throw ApiException.badRequest("INVALID_FACULTY", "La facultad necesita código y nombre.");
        }
        return faculties.save(faculty);
    }

    @PostMapping("/admin/careers")
    @Transactional
    public Career createCareer(@RequestBody CareerBody body) {
        Career career = new Career();
        career.setCode(Texts.limit(body.code(), 40));
        career.setName(Texts.limit(body.name(), 180));
        career.setNameEn(Texts.limit(body.nameEn(), 180));
        career.setIscedCode(Texts.limit(body.iscedCode(), 10));
        career.setDescription(Texts.limit(body.description(), 1000));
        if (career.getCode() == null || career.getName() == null) {
            throw ApiException.badRequest("INVALID_CAREER", "La carrera necesita código y nombre.");
        }
        if (body.facultyId() != null) {
            faculties.findById(body.facultyId()).ifPresent(faculty -> career.getFaculties().add(faculty));
        }
        return careers.save(career);
    }

    @PatchMapping("/admin/careers/{id}")
    @Transactional
    public Career updateCareer(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        Career career = careers.findById(id).orElseThrow(() -> ApiException.notFound("No encontramos esa carrera."));
        if (body.containsKey("name") && body.get("name") != null) {
            career.setName(Texts.limit(body.get("name").toString(), 180));
        }
        if (body.containsKey("active")) {
            career.setActive(Boolean.parseBoolean(body.get("active").toString()));
        }
        return career;
    }

    @PostMapping("/admin/universities")
    public University createUniversity(@RequestBody UniversityBody body) {
        University university = new University();
        university.setName(Texts.limit(body.name(), 220));
        university.setCity(Texts.limit(body.city(), 120));
        university.setWebsite(Texts.cleanLink(body.website()));
        if (university.getName() == null) {
            throw ApiException.badRequest("INVALID_UNIVERSITY", "La universidad necesita un nombre.");
        }
        return universities.save(university);
    }

    @GetMapping("/admin/template-configs")
    public Object configs(@RequestParam(required = false) UUID careerId) {
        return careerId == null ? configs.findByCareerIdIsNullAndActiveTrueOrderBySortOrderAsc() : configs.findByCareerIdAndActiveTrueOrderBySortOrderAsc(careerId);
    }

    @PatchMapping("/admin/template-configs/{id}")
    @Transactional
    public CareerTemplateConfig updateConfig(@PathVariable UUID id, @RequestBody JsonNode body) {
        CareerTemplateConfig config = configs.findById(id).orElseThrow(() -> ApiException.notFound("No encontramos esa configuración."));
        if (!body.has("sections") || !body.get("sections").isArray()) {
            throw ApiException.badRequest("INVALID_SCHEMA", "El esquema debe incluir un arreglo sections.");
        }
        config.setSectionSchema(body.get("sections"));
        config.setGenerated(false);
        if (body.has("disclaimer")) {
            config.setDisclaimer(body.get("disclaimer").isNull() ? null : Texts.limit(body.get("disclaimer").asText(), 1000));
        }
        return config;
    }

    @GetMapping("/admin/audit")
    public Object audit(@RequestParam(defaultValue = "0") int page) {
        return auditLogs.findAllByOrderByCreatedAtDesc(PageRequest.of(page, 30));
    }

    @GetMapping("/admin/reports")
    public Map<String, Long> reports() {
        return Map.of(
                "users", users.count(),
                "careers", careers.count(),
                "portfolios", portfolios.count(),
                "faculties", faculties.count()
        );
    }

    @PostMapping("/moderation/portfolios/{id}/unpublish")
    public Map<String, Object> moderate(@PathVariable UUID id, @RequestBody(required = false) Map<String, String> body, HttpServletRequest request) {
        return portfolioService.moderate(id, body == null ? null : body.get("reason"), clientIp.resolve(request));
    }

    public record FacultyBody(String code, String name, String nameEn, String description) {
    }

    public record CareerBody(String code, String name, String nameEn, String iscedCode, String description, UUID facultyId) {
    }

    public record UniversityBody(String name, String city, String website) {
    }
}
