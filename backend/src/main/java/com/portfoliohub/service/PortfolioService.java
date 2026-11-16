package com.portfoliohub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.portfoliohub.common.ApiException;
import com.portfoliohub.common.Texts;
import com.portfoliohub.config.AppProperties;
import com.portfoliohub.domain.AcademicRecord;
import com.portfoliohub.domain.Career;
import com.portfoliohub.domain.CareerTemplateConfig;
import com.portfoliohub.domain.Certification;
import com.portfoliohub.domain.Course;
import com.portfoliohub.domain.Experience;
import com.portfoliohub.domain.Faculty;
import com.portfoliohub.domain.PersonalProfile;
import com.portfoliohub.domain.Portfolio;
import com.portfoliohub.domain.PortfolioDomain;
import com.portfoliohub.domain.PortfolioPublication;
import com.portfoliohub.domain.PortfolioSection;
import com.portfoliohub.domain.PortfolioStatus;
import com.portfoliohub.domain.PortfolioVersion;
import com.portfoliohub.domain.PortfolioVisit;
import com.portfoliohub.domain.ProfessionalProfile;
import com.portfoliohub.domain.Project;
import com.portfoliohub.domain.ProjectStatus;
import com.portfoliohub.domain.Publication;
import com.portfoliohub.domain.Skill;
import com.portfoliohub.domain.StoredDocument;
import com.portfoliohub.domain.TemplateDefinition;
import com.portfoliohub.domain.User;
import com.portfoliohub.domain.UserSkill;
import com.portfoliohub.repo.AcademicRecordRepository;
import com.portfoliohub.repo.CareerRepository;
import com.portfoliohub.repo.CareerTemplateConfigRepository;
import com.portfoliohub.repo.CertificationRepository;
import com.portfoliohub.repo.CityRepository;
import com.portfoliohub.repo.ContactMessageRepository;
import com.portfoliohub.repo.CountryRepository;
import com.portfoliohub.repo.CourseRepository;
import com.portfoliohub.repo.DistrictRepository;
import com.portfoliohub.repo.ExperienceRepository;
import com.portfoliohub.repo.FacultyRepository;
import com.portfoliohub.repo.PersonalProfileRepository;
import com.portfoliohub.repo.PortfolioDomainRepository;
import com.portfoliohub.repo.PortfolioPublicationRepository;
import com.portfoliohub.repo.PortfolioRepository;
import com.portfoliohub.repo.PortfolioSectionRepository;
import com.portfoliohub.repo.PortfolioVersionRepository;
import com.portfoliohub.repo.PortfolioVisitRepository;
import com.portfoliohub.repo.ProfessionalProfileRepository;
import com.portfoliohub.repo.ProjectImageRepository;
import com.portfoliohub.repo.ProjectRepository;
import com.portfoliohub.repo.ProjectTechnologyRepository;
import com.portfoliohub.repo.PublicationRepository;
import com.portfoliohub.repo.SkillRepository;
import com.portfoliohub.repo.StoredDocumentRepository;
import com.portfoliohub.repo.TemplateDefinitionRepository;
import com.portfoliohub.repo.UserRepository;
import com.portfoliohub.repo.UserSkillRepository;
import com.portfoliohub.domain.ContactMessage;
import com.portfoliohub.domain.DocumentVisibility;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class PortfolioService {

    private static final Set<String> FONTS = Set.of(
            "Fraunces", "Outfit", "Libre Baskerville", "Source Sans 3", "Space Grotesk", "IBM Plex Sans",
            "Newsreader", "Sora", "Manrope", "Source Serif 4", "Cormorant Garamond", "IBM Plex Mono"
    );
    private static final Set<String> SOFTWARE = Set.of("ING-SIS", "ING-INF", "ING-SW", "CIEN-DATOS", "INTEL-ART", "CIBERSEG", "COMP-CIEN");

    private final PortfolioRepository portfolios;
    private final PortfolioSectionRepository sections;
    private final PortfolioVersionRepository versions;
    private final PortfolioPublicationRepository publications;
    private final PortfolioDomainRepository domains;
    private final PortfolioVisitRepository visits;
    private final TemplateDefinitionRepository templates;
    private final CareerTemplateConfigRepository configs;
    private final UserRepository users;
    private final PersonalProfileRepository personalProfiles;
    private final ProfessionalProfileRepository professionalProfiles;
    private final AcademicRecordRepository academicRecords;
    private final CareerRepository careers;
    private final FacultyRepository faculties;
    private final ProjectRepository projects;
    private final ProjectTechnologyRepository technologies;
    private final ProjectImageRepository images;
    private final CourseRepository courses;
    private final CertificationRepository certifications;
    private final ExperienceRepository experiences;
    private final PublicationRepository publicationItems;
    private final UserSkillRepository userSkills;
    private final SkillRepository skills;
    private final StoredDocumentRepository documents;
    private final CountryRepository countries;
    private final CityRepository cities;
    private final DistrictRepository districts;
    private final ContactMessageRepository contacts;
    private final NotificationService notifications;
    private final AuditService audit;
    private final AppProperties properties;
    private final ObjectMapper mapper;

    public PortfolioService(
            PortfolioRepository portfolios,
            PortfolioSectionRepository sections,
            PortfolioVersionRepository versions,
            PortfolioPublicationRepository publications,
            PortfolioDomainRepository domains,
            PortfolioVisitRepository visits,
            TemplateDefinitionRepository templates,
            CareerTemplateConfigRepository configs,
            UserRepository users,
            PersonalProfileRepository personalProfiles,
            ProfessionalProfileRepository professionalProfiles,
            AcademicRecordRepository academicRecords,
            CareerRepository careers,
            FacultyRepository faculties,
            ProjectRepository projects,
            ProjectTechnologyRepository technologies,
            ProjectImageRepository images,
            CourseRepository courses,
            CertificationRepository certifications,
            ExperienceRepository experiences,
            PublicationRepository publicationItems,
            UserSkillRepository userSkills,
            SkillRepository skills,
            StoredDocumentRepository documents,
            CountryRepository countries,
            CityRepository cities,
            DistrictRepository districts,
            ContactMessageRepository contacts,
            NotificationService notifications,
            AuditService audit,
            AppProperties properties,
            ObjectMapper mapper
    ) {
        this.portfolios = portfolios;
        this.sections = sections;
        this.versions = versions;
        this.publications = publications;
        this.domains = domains;
        this.visits = visits;
        this.templates = templates;
        this.configs = configs;
        this.users = users;
        this.personalProfiles = personalProfiles;
        this.professionalProfiles = professionalProfiles;
        this.academicRecords = academicRecords;
        this.careers = careers;
        this.faculties = faculties;
        this.projects = projects;
        this.technologies = technologies;
        this.images = images;
        this.courses = courses;
        this.certifications = certifications;
        this.experiences = experiences;
        this.publicationItems = publicationItems;
        this.userSkills = userSkills;
        this.skills = skills;
        this.documents = documents;
        this.countries = countries;
        this.cities = cities;
        this.districts = districts;
        this.contacts = contacts;
        this.notifications = notifications;
        this.audit = audit;
        this.properties = properties;
        this.mapper = mapper;
    }

    @Transactional
    public Portfolio ensure(UUID userId) {
        return portfolios.findByUserId(userId).orElseGet(() -> create(userId));
    }

    @Transactional
    public Portfolio syncAcademic(UUID userId) {
        Portfolio existing = portfolios.findByUserId(userId).orElse(null);
        if (existing == null) {
            return create(userId);
        }
        AcademicRecord academic = academicRecords.findFirstByUserIdAndPrimaryRecordTrueOrderByUpdatedAtDesc(userId).orElse(null);
        UUID careerId = academic == null ? null : academic.getCareerId();
        if (Objects.equals(existing.getCareerId(), careerId)) {
            return existing;
        }
        existing.setCareerId(careerId);
        Career career = careerId == null ? null : careers.findById(careerId).orElse(null);
        if (!existing.isThemeTouched()) {
            TemplateDefinition template = preferredTemplate(career);
            CareerTemplateConfig config = configFor(careerId, template.getId());
            existing.setTemplateId(template.getId());
            existing.setTheme(mergedTheme(template, config));
            mergeSections(existing, config);
        } else if (existing.getTemplateId() != null) {
            mergeSections(existing, configFor(careerId, existing.getTemplateId()));
        }
        save(existing);
        return existing;
    }

    @Transactional
    public Map<String, Object> editor(UUID userId) {
        Portfolio portfolio = ensure(userId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("portfolio", summary(portfolio));
        body.put("sections", sections.findByPortfolioIdOrderBySortOrderAsc(portfolio.getId()));
        body.put("versions", versions.findTop30ByPortfolioIdOrderByCreatedAtDesc(portfolio.getId()).stream().map(version -> Map.of(
                "id", version.getId(),
                "label", version.getLabel() == null ? "" : version.getLabel(),
                "createdAt", version.getCreatedAt()
        )).toList());
        body.put("domains", domains.findByPortfolioIdOrderByCreatedAtDesc(portfolio.getId()));
        return body;
    }

    @Transactional
    public Map<String, Object> preview(UUID userId) {
        return assemble(ensure(userId), true);
    }

    @Transactional
    public Map<String, Object> updateSettings(UUID userId, SettingsUpdate update) {
        Portfolio portfolio = ensure(userId);
        checkVersion(portfolio, update.version());
        if (update.title() != null) {
            String title = Texts.limit(update.title(), 160);
            if (title == null) {
                throw ApiException.badRequest("TITLE_REQUIRED", "El portafolio necesita un título.");
            }
            portfolio.setTitle(title);
        }
        if (update.theme() != null) {
            portfolio.setTheme(sanitizeTheme(update.theme(), portfolio.getTheme()));
            portfolio.setThemeTouched(true);
        }
        portfolio.setBilingual(update.bilingual());
        portfolio.setWhatsappEnabled(update.whatsappEnabled());
        portfolio.setShowEmail(update.showEmail());
        portfolio.setShowPhone(update.showPhone());
        portfolio.setAnalyticsEnabled(update.analyticsEnabled());
        portfolio.setRecruiterViewEnabled(update.recruiterViewEnabled());
        portfolio.setShowcaseOptIn(update.showcaseOptIn());
        portfolio.setSeoTitle(Texts.limit(update.seoTitle(), 160));
        portfolio.setSeoDescription(Texts.limit(update.seoDescription(), 300));
        save(portfolio);
        return summary(portfolio);
    }

    @Transactional
    public Map<String, Object> updateSections(UUID userId, long version, List<SectionUpdate> incoming) {
        Portfolio portfolio = ensure(userId);
        checkVersion(portfolio, version);
        Map<String, PortfolioSection> current = new LinkedHashMap<>();
        sections.findByPortfolioIdOrderBySortOrderAsc(portfolio.getId()).forEach(section -> current.put(section.getSectionKey(), section));
        for (SectionUpdate item : incoming) {
            PortfolioSection section = current.get(item.key());
            if (section == null || item.key() == null || !item.key().matches("[a-z0-9_]+")) {
                continue;
            }
            String title = Texts.limit(item.title(), 160);
            if (title != null) {
                section.setTitle(title);
            }
            section.setVisible(item.visible());
            section.setSortOrder(item.sortOrder());
            if (item.content() != null) {
                if (item.content().toString().length() > 20000) {
                    throw ApiException.badRequest("CONTENT_TOO_LARGE", "Una sección tiene demasiado texto.");
                }
                section.setContent(item.content());
            }
        }
        save(portfolio);
        return summary(portfolio);
    }

    @Transactional
    public Map<String, Object> applyTemplate(UUID userId, UUID templateId) {
        Portfolio portfolio = ensure(userId);
        TemplateDefinition template = templates.findById(templateId).filter(TemplateDefinition::isActive)
                .orElseThrow(() -> ApiException.notFound("No encontramos esa plantilla."));
        snapshot(portfolio, "Antes de cambiar a " + template.getName());
        CareerTemplateConfig config = configFor(portfolio.getCareerId(), template.getId());
        portfolio.setTemplateId(template.getId());
        if (!portfolio.isThemeTouched()) {
            portfolio.setTheme(mergedTheme(template, config));
        }
        mergeSections(portfolio, config);
        save(portfolio);
        return editor(userId);
    }

    @Transactional
    public Map<String, Object> deleteSection(UUID userId, String key) {
        Portfolio portfolio = ensure(userId);
        snapshot(portfolio, "Antes de eliminar la sección " + key);
        sections.deleteByPortfolioIdAndSectionKey(portfolio.getId(), key);
        save(portfolio);
        return editor(userId);
    }

    @Transactional
    public PortfolioVersion saveVersion(UUID userId, String label) {
        Portfolio portfolio = ensure(userId);
        return snapshot(portfolio, Texts.limit(label, 160) == null ? "Versión guardada" : Texts.limit(label, 160));
    }

    @Transactional
    public Map<String, Object> restore(UUID userId, UUID versionId) {
        Portfolio portfolio = ensure(userId);
        PortfolioVersion version = versions.findById(versionId)
                .filter(item -> item.getPortfolioId().equals(portfolio.getId()))
                .orElseThrow(() -> ApiException.notFound("No encontramos esa versión."));
        snapshot(portfolio, "Antes de restaurar");
        JsonNode snapshot = version.getSnapshot();
        if (snapshot.hasNonNull("templateId")) {
            portfolio.setTemplateId(UUID.fromString(snapshot.get("templateId").asText()));
        }
        if (snapshot.has("theme")) {
            portfolio.setTheme(snapshot.get("theme"));
        }
        portfolio.setThemeTouched(snapshot.path("themeTouched").asBoolean(true));
        sections.findByPortfolioIdOrderBySortOrderAsc(portfolio.getId()).forEach(sections::delete);
        for (JsonNode node : snapshot.path("sections")) {
            PortfolioSection section = new PortfolioSection();
            section.setPortfolioId(portfolio.getId());
            section.setSectionKey(node.path("key").asText());
            section.setTitle(node.path("title").asText("Sección"));
            section.setComponent(node.path("component").asText("richtext"));
            section.setVariant(node.path("variant").asText(null));
            section.setVisible(node.path("visible").asBoolean(true));
            section.setSortOrder(node.path("sortOrder").asInt());
            section.setContent(node.has("content") ? node.get("content") : mapper.createObjectNode());
            sections.save(section);
        }
        save(portfolio);
        return editor(userId);
    }

    @Transactional
    public Map<String, Object> slugAvailability(UUID userId, String slug) {
        String normalized = Texts.slug(slug);
        Texts.checkSlug(normalized);
        Portfolio own = ensure(userId);
        boolean taken = portfolios.findBySlugIgnoreCase(normalized)
                .filter(found -> !found.getId().equals(own.getId()))
                .isPresent();
        return Map.of("slug", normalized, "available", !taken);
    }

    @Transactional
    public Map<String, Object> publish(UUID userId, String slug, boolean accept, String seoTitle, String seoDescription, String ip) {
        if (!accept) {
            throw ApiException.badRequest("CONSENT_REQUIRED", "La publicación requiere tu consentimiento explícito.");
        }
        Portfolio portfolio = ensure(userId);
        String normalized = Texts.slug(slug == null || slug.isBlank() ? portfolio.getTitle() : slug);
        Texts.checkSlug(normalized);
        if (portfolios.findBySlugIgnoreCase(normalized).filter(found -> !found.getId().equals(portfolio.getId())).isPresent()) {
            throw ApiException.conflict("SLUG_TAKEN", "Ese identificador ya está en uso.");
        }
        List<String> missing = publishGaps(userId);
        if (!missing.isEmpty()) {
            throw new ApiException(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "INCOMPLETE", String.join(" ", missing));
        }
        portfolio.setSlug(normalized);
        portfolio.setSeoTitle(Texts.limit(seoTitle, 160));
        portfolio.setSeoDescription(Texts.limit(seoDescription, 300));
        portfolio.setConsentToPublish(true);
        portfolio.setStatus(PortfolioStatus.PUBLISHED);
        portfolio.setPublishedAt(Instant.now());
        try {
            save(portfolio);
        } catch (DataIntegrityViolationException exception) {
            throw ApiException.conflict("SLUG_TAKEN", "Ese identificador ya está en uso.");
        }
        PortfolioPublication publication = new PortfolioPublication();
        publication.setPortfolioId(portfolio.getId());
        publication.setSlug(normalized);
        publication.setPublishedAt(portfolio.getPublishedAt());
        publications.save(publication);
        notifications.notify(userId, "Portafolio publicado", "Tu portafolio ya está disponible en /p/" + normalized + ".");
        audit.record(userId, "PUBLISH", "PORTFOLIO", portfolio.getId().toString(), normalized, ip);
        return summary(portfolio);
    }

    @Transactional
    public Map<String, Object> unpublish(UUID userId, String reason, String ip) {
        return closePublication(ensure(userId), reason, ip, false);
    }

    @Transactional
    public Map<String, Object> moderate(UUID portfolioId, String reason, String ip) {
        Portfolio portfolio = portfolios.findById(portfolioId).orElseThrow(() -> ApiException.notFound("No encontramos ese portafolio."));
        return closePublication(portfolio, reason, ip, true);
    }

    private Map<String, Object> closePublication(Portfolio portfolio, String reason, String ip, boolean moderator) {
        if (portfolio.getStatus() != PortfolioStatus.PUBLISHED) {
            throw ApiException.badRequest("NOT_PUBLISHED", "El portafolio no está publicado.");
        }
        portfolio.setStatus(PortfolioStatus.UNPUBLISHED);
        save(portfolio);
        publications.findByPortfolioIdOrderByPublishedAtDesc(portfolio.getId()).stream().findFirst().ifPresent(item -> {
            item.setUnpublishedAt(Instant.now());
            item.setUnpublishedReason(Texts.limit(reason, 255));
        });
        notifications.notify(portfolio.getUserId(), "Portafolio despublicado", "El sitio público dejó de estar disponible.");
        audit.record(moderator ? null : portfolio.getUserId(), "UNPUBLISH", "PORTFOLIO", portfolio.getId().toString(), Texts.limit(reason, 255), ip);
        return summary(portfolio);
    }

    @Transactional
    public PortfolioDomain registerDomain(UUID userId, String domain) {
        Portfolio portfolio = ensure(userId);
        String value = Texts.blankToNull(domain);
        if (value == null || !value.matches("(?i)^[a-z0-9.-]+\\.[a-z]{2,}$") || value.length() > 255) {
            throw ApiException.badRequest("INVALID_DOMAIN", "Escribe un dominio como tuportafolio.com.");
        }
        PortfolioDomain item = new PortfolioDomain();
        item.setPortfolioId(portfolio.getId());
        item.setDomain(value.toLowerCase(Locale.ROOT));
        item.setStatus("REGISTERED_INTEREST");
        return domains.save(item);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> publicView(String slug) {
        Portfolio portfolio = portfolios.findBySlugIgnoreCase(slug)
                .filter(item -> item.getStatus() == PortfolioStatus.PUBLISHED)
                .orElseThrow(() -> ApiException.notFound("No encontramos ese portafolio."));
        return assemble(portfolio, false);
    }

    @Transactional
    public void visit(String slug, boolean consent, String path, String device, String referrer) {
        if (!consent) {
            throw ApiException.badRequest("CONSENT_REQUIRED", "La visita solo se registra con consentimiento.");
        }
        Portfolio portfolio = portfolios.findBySlugIgnoreCase(slug)
                .filter(item -> item.getStatus() == PortfolioStatus.PUBLISHED)
                .orElseThrow(() -> ApiException.notFound("No encontramos ese portafolio."));
        if (!portfolio.isAnalyticsEnabled()) {
            return;
        }
        PortfolioVisit visit = new PortfolioVisit();
        visit.setPortfolioId(portfolio.getId());
        visit.setVisitedAt(Instant.now());
        visit.setPath(Texts.limit(path, 160));
        String normalizedDevice = device == null ? "desktop" : device.toLowerCase(Locale.ROOT);
        visit.setDevice(Set.of("mobile", "tablet", "desktop").contains(normalizedDevice) ? normalizedDevice : "desktop");
        visit.setReferrer(Texts.cleanLink(referrer));
        visits.save(visit);
    }

    @Transactional
    public void contact(String slug, String name, String email, String message, String honeypot) {
        if (honeypot != null && !honeypot.isBlank()) {
            return;
        }
        String cleanName = Texts.limit(name, 120);
        String cleanEmail = Texts.blankToNull(email);
        String cleanMessage = Texts.limit(message, 4000);
        if (cleanName == null || cleanEmail == null || !cleanEmail.matches("(?i)^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || cleanMessage == null || cleanMessage.length() < 10) {
            throw ApiException.badRequest("INVALID_CONTACT", "Revisa el nombre, el correo y el mensaje.");
        }
        Portfolio portfolio = portfolios.findBySlugIgnoreCase(slug)
                .filter(item -> item.getStatus() == PortfolioStatus.PUBLISHED)
                .orElseThrow(() -> ApiException.notFound("No encontramos ese portafolio."));
        ContactMessage item = new ContactMessage();
        item.setPortfolioId(portfolio.getId());
        item.setName(cleanName);
        item.setEmail(cleanEmail.toLowerCase(Locale.ROOT));
        item.setMessage(cleanMessage);
        contacts.save(item);
        notifications.notify(portfolio.getUserId(), "Nuevo mensaje de contacto", cleanName + " escribió desde tu portafolio.");
    }

    @Transactional
    public Portfolio openSite(UUID userId) {
        Portfolio portfolio = ensure(userId);
        if (portfolio.getSlug() == null || portfolio.getSlug().isBlank()) {
            portfolio.setSlug(uniqueSlug(portfolio.getTitle(), portfolio.getId()));
        }
        if (portfolio.getStatus() == PortfolioStatus.DRAFT) {
            portfolio.setStatus(PortfolioStatus.PUBLISHED);
            portfolio.setPublishedAt(Instant.now());
            portfolio.setConsentToPublish(true);
        }
        save(portfolio);
        return portfolio;
    }

    @Transactional
    public Map<String, Object> dashboard(UUID userId) {
        Portfolio portfolio = openSite(userId);
        PersonalProfile personal = personalProfiles.findById(userId).orElseThrow();
        AcademicRecord academic = academicRecords.findFirstByUserIdAndPrimaryRecordTrueOrderByUpdatedAtDesc(userId).orElse(null);
        int score = 0;
        List<String> recommendations = new ArrayList<>();
        if (personal.getBio() != null && personal.getBio().length() >= 40) {
            score += 15;
        } else {
            recommendations.add("Escribe una biografía de al menos un párrafo.");
        }
        if (personal.getPhotoDocumentId() != null) {
            score += 10;
        } else {
            recommendations.add("Añade una fotografía profesional.");
        }
        if (personal.getCityId() != null || personal.getCustomCity() != null || personal.getCustomDistrict() != null) {
            score += 10;
        } else {
            recommendations.add("Indica tu ciudad. Si no está en el catálogo, escríbela manualmente.");
        }
        boolean hasCareer = academic != null && (academic.getCareerId() != null || (academic.getCareerName() != null && !academic.getCareerName().isBlank()));
        if (hasCareer) {
            score += 15;
        } else {
            recommendations.add("Selecciona tu carrera para recibir secciones especializadas.");
        }
        if (!userSkills.findByUserId(userId).isEmpty()) {
            score += 10;
        } else {
            recommendations.add("Registra habilidades por categoría, sin porcentajes.");
        }
        if (!projects.findByUserIdOrderByCreatedAtDesc(userId).isEmpty()) {
            score += 10;
        } else {
            recommendations.add("Añade al menos un proyecto universitario o personal.");
        }
        if (!courses.findByUserIdOrderByCreatedAtDesc(userId).isEmpty() || !certifications.findByUserIdOrderByCreatedAtDesc(userId).isEmpty()) {
            score += 10;
        } else {
            recommendations.add("Registra un curso o una certificación revisada por ti.");
        }
        if (personal.getPhone() != null || personal.getProfessionalEmail() != null || professionalProfiles.findById(userId).map(ProfessionalProfile::getLinkedin).orElse(null) != null) {
            score += 10;
        } else {
            recommendations.add("Agrega un medio de contacto.");
        }
        if (portfolio.getTemplateId() != null) {
            score += 10;
        }
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("completion", score);
        body.put("recommendations", recommendations);
        body.put("publicationStatus", portfolio.getStatus());
        body.put("slug", portfolio.getSlug());
        body.put("publicUrl", portfolio.getStatus() == PortfolioStatus.PUBLISHED ? properties.getPublicBaseUrl() + "/p/" + portfolio.getSlug() : null);
        body.put("visits", visits.countByPortfolioId(portfolio.getId()));
        body.put("visitsByDevice", visits.countByDevice(portfolio.getId(), since));
        body.put("projects", projects.findByUserIdOrderByCreatedAtDesc(userId).size());
        body.put("courses", courses.findByUserIdOrderByCreatedAtDesc(userId).size());
        body.put("certifications", certifications.findByUserIdOrderByCreatedAtDesc(userId).size());
        body.put("updatedAt", portfolio.getUpdatedAt());
        User user = users.findById(userId).orElseThrow();
        Career career = academic != null && academic.getCareerId() != null ? careers.findById(academic.getCareerId()).orElse(null) : null;
        body.put("firstName", user.getFirstName());
        body.put("lastName", user.getLastName());
        body.put("career", academic == null ? "" : (career != null ? career.getName() : academic.getCareerName()));
        body.put("location", location(personal));
        return body;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> examples() {
        return portfolios.findByStatusAndShowcaseOptInTrue(PortfolioStatus.PUBLISHED, org.springframework.data.domain.PageRequest.of(0, 12))
                .map(portfolio -> {
                    User user = users.findById(portfolio.getUserId()).orElse(null);
                    Map<String, Object> card = new LinkedHashMap<>();
                    card.put("slug", portfolio.getSlug());
                    card.put("title", portfolio.getTitle());
                    card.put("name", user == null ? portfolio.getTitle() : user.getFirstName() + " " + user.getLastName());
                    card.put("seoDescription", portfolio.getSeoDescription());
                    return card;
                }).getContent();
    }

    public String publicUrl(Portfolio portfolio) {
        if (portfolio.getSlug() == null) {
            return null;
        }
        return properties.getPublicBaseUrl() + "/p/" + portfolio.getSlug();
    }

    private String uniqueSlug(String title, UUID self) {
        String base = Texts.slug(title);
        if (base.length() < 3) {
            base = "sitio";
        }
        String candidate = base;
        int suffix = 2;
        while (slugTaken(candidate, self)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private boolean slugTaken(String candidate, UUID self) {
        try {
            Texts.checkSlug(candidate);
        } catch (ApiException exception) {
            return true;
        }
        return portfolios.findBySlugIgnoreCase(candidate)
                .filter(found -> self == null || !found.getId().equals(self))
                .isPresent();
    }

    private Portfolio create(UUID userId) {
        User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("No encontramos tu cuenta."));
        AcademicRecord academic = academicRecords.findFirstByUserIdAndPrimaryRecordTrueOrderByUpdatedAtDesc(userId).orElse(null);
        UUID careerId = academic == null ? null : academic.getCareerId();
        Career career = careerId == null ? null : careers.findById(careerId).orElse(null);
        TemplateDefinition template = preferredTemplate(career);
        CareerTemplateConfig config = configFor(careerId, template.getId());
        Portfolio portfolio = new Portfolio();
        portfolio.setUserId(userId);
        portfolio.setTitle(user.getFirstName() + " " + user.getLastName());
        portfolio.setStatus(PortfolioStatus.DRAFT);
        portfolio.setTemplateId(template.getId());
        portfolio.setCareerId(careerId);
        portfolio.setTheme(mergedTheme(template, config));
        portfolio.setRecruiterViewEnabled(true);
        portfolios.save(portfolio);
        mergeSections(portfolio, config);
        return portfolio;
    }

    private TemplateDefinition preferredTemplate(Career career) {
        String code = "premium";
        if (career != null) {
            if (SOFTWARE.contains(career.getCode())) {
                code = "tech";
            } else if ("MED-HUM".equals(career.getCode()) || "PSICO".equals(career.getCode())) {
                code = "academic";
            } else if ("DERECHO".equals(career.getCode())) {
                code = "editorial";
            } else if ("ARQ".equals(career.getCode()) || "DIS-GRAF".equals(career.getCode())) {
                code = "creative";
            } else if ("ADMIN".equals(career.getCode()) || "MARKET".equals(career.getCode())) {
                code = "corporate";
            }
        }
        return templates.findByCode(code).orElseGet(() -> templates.findByActiveTrueOrderBySortOrderAsc().get(0));
    }

    private CareerTemplateConfig configFor(UUID careerId, UUID templateId) {
        if (careerId != null) {
            CareerTemplateConfig found = configs.findByCareerIdAndTemplateId(careerId, templateId).orElse(null);
            if (found != null) {
                return found;
            }
        }
        return configs.findByCareerIdIsNullAndTemplateId(templateId).orElse(null);
    }

    private JsonNode mergedTheme(TemplateDefinition template, CareerTemplateConfig config) {
        ObjectNode theme = template.getThemeDefaults().deepCopy();
        if (config != null && config.getThemeOverrides() != null && config.getThemeOverrides().hasNonNull("accent")) {
            theme.put("accent", config.getThemeOverrides().get("accent").asText());
        }
        return theme;
    }

    private void mergeSections(Portfolio portfolio, CareerTemplateConfig config) {
        JsonNode schema = config == null ? mapper.createArrayNode() : config.getSectionSchema();
        Map<String, PortfolioSection> current = new LinkedHashMap<>();
        sections.findByPortfolioIdOrderBySortOrderAsc(portfolio.getId()).forEach(section -> current.put(section.getSectionKey(), section));
        int order = 0;
        for (JsonNode node : schema) {
            String key = node.path("key").asText();
            PortfolioSection section = current.get(key);
            if (section == null) {
                section = new PortfolioSection();
                section.setPortfolioId(portfolio.getId());
                section.setSectionKey(key);
                section.setVisible(node.path("recommended").asBoolean(true));
                section.setContent(mapper.createObjectNode());
                current.put(key, section);
            }
            section.setTitle(node.path("title").asText(section.getTitle() == null ? "Sección" : section.getTitle()));
            section.setComponent(node.path("component").asText("richtext"));
            section.setVariant(node.path("variant").asText("general"));
            section.setSortOrder(order++);
            sections.save(section);
        }
    }

    private Map<String, Object> assemble(Portfolio portfolio, boolean preview) {
        User user = users.findById(portfolio.getUserId()).orElseThrow();
        PersonalProfile personal = personalProfiles.findById(user.getId()).orElseThrow();
        ProfessionalProfile professional = professionalProfiles.findById(user.getId()).orElseThrow();
        AcademicRecord academic = academicRecords.findFirstByUserIdAndPrimaryRecordTrueOrderByUpdatedAtDesc(user.getId()).orElse(null);
        Career career = academic != null && academic.getCareerId() != null ? careers.findById(academic.getCareerId()).orElse(null) : null;
        TemplateDefinition template = portfolio.getTemplateId() == null ? null : templates.findById(portfolio.getTemplateId()).orElse(null);
        CareerTemplateConfig config = configFor(portfolio.getCareerId(), portfolio.getTemplateId());
        List<PortfolioSection> visible = sections.findByPortfolioIdOrderBySortOrderAsc(portfolio.getId()).stream()
                .filter(PortfolioSection::isVisible)
                .toList();
        Set<String> hidden = new java.util.HashSet<>();
        for (PortfolioSection section : visible) {
            JsonNode ids = section.getContent() == null ? null : section.getContent().get("hiddenIds");
            if (ids != null && ids.isArray()) {
                ids.forEach(id -> hidden.add(id.asText()));
            }
        }
        List<Map<String, Object>> projectViews = projects.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(project -> preview || project.getPublicationStatus() == ProjectStatus.PUBLISHED)
                .filter(project -> !hidden.contains(project.getId().toString()))
                .map(this::projectView)
                .toList();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("preview", preview);
        body.put("slug", portfolio.getSlug());
        body.put("title", portfolio.getTitle());
        body.put("layout", template == null ? "minimal" : template.getLayout());
        body.put("templateCode", template == null ? "minimal" : template.getCode());
        body.put("theme", portfolio.getTheme());
        body.put("bilingual", portfolio.isBilingual());
        body.put("recruiterViewEnabled", portfolio.isRecruiterViewEnabled());
        body.put("disclaimer", config == null ? null : config.getDisclaimer());
        body.put("studentSafe", config != null && config.isStudentSafe());
        String whatsapp = personal.getPhone() == null ? "" : personal.getPhone().replaceAll("\\D", "");
        body.put("whatsappEnabled", !whatsapp.isEmpty());
        body.put("whatsappNumber", whatsapp.isEmpty() ? null : whatsapp);
        body.put("sections", visible);
        body.put("person", personView(user, personal, professional, portfolio));
        body.put("academicLabel", academicLabel(academic, career, config));
        body.put("education", academic == null ? null : education(academic, career));
        body.put("projects", projectViews);
        body.put("courses", courses.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().filter(item -> !hidden.contains(item.getId().toString())).map(this::courseView).toList());
        body.put("certifications", certifications.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().filter(item -> !hidden.contains(item.getId().toString())).map(this::certificationView).toList());
        body.put("experiences", experiences.findByUserIdOrderByStartDateDesc(user.getId()).stream()
                .filter(item -> !hidden.contains(item.getId().toString()))
                .map(this::experienceView)
                .toList());
        body.put("publications", publicationItems.findByUserIdOrderByPublishedOnDesc(user.getId()).stream().filter(item -> !hidden.contains(item.getId().toString())).map(this::publicationView).toList());
        body.put("skills", skillViews(user.getId(), hidden));
        body.put("seo", seo(portfolio, user, personal));
        return body;
    }

    private Map<String, Object> personView(User user, PersonalProfile personal, ProfessionalProfile professional, Portfolio portfolio) {
        Map<String, Object> person = new LinkedHashMap<>();
        person.put("firstName", user.getFirstName());
        person.put("lastName", user.getLastName());
        person.put("bio", personal.getBio());
        person.put("bioEn", personal.getBioEn());
        person.put("photoUrl", publicFile(personal.getPhotoDocumentId()));
        person.put("location", location(personal));
        person.put("email", portfolio.isShowEmail() ? (personal.getProfessionalEmail() != null ? personal.getProfessionalEmail() : user.getEmail()) : null);
        person.put("phone", portfolio.isShowPhone() ? personal.getPhone() : null);
        if (personal.isShowBirthDate()) {
            person.put("birthDate", personal.getBirthDate());
        }
        List<Map<String, String>> links = new ArrayList<>();
        addLink(links, "LinkedIn", professional.getLinkedin());
        addLink(links, "GitHub", professional.getGithub());
        addLink(links, "GitLab", professional.getGitlab());
        addLink(links, "Behance", professional.getBehance());
        addLink(links, "Dribbble", professional.getDribbble());
        addLink(links, "ORCID", professional.getOrcid());
        addLink(links, "Google Scholar", professional.getGoogleScholar());
        addLink(links, "Sitio web", professional.getWebsite());
        if (professional.getOtherLinks() != null && professional.getOtherLinks().isArray()) {
            for (JsonNode link : professional.getOtherLinks()) {
                addLink(links, link.path("label").asText("Enlace"), link.path("url").asText(null));
            }
        }
        person.put("links", links);
        return person;
    }

    private void addLink(List<Map<String, String>> links, String label, String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        if (url.contains("://") && !(url.startsWith("https://") || url.startsWith("http://"))) {
            return;
        }
        links.add(Map.of("label", label, "url", url));
    }

    private String academicLabel(AcademicRecord academic, Career career, CareerTemplateConfig config) {
        if (academic == null) {
            return null;
        }
        String name = career != null ? career.getName() : academic.getCareerName();
        String label = Texts.academicLabel(academic.getStatus(), name, config != null && config.isStudentSafe(), academic.isLicenseDeclared());
        if (config != null && config.isStudentSafe() && (academic.getStatus() == com.portfoliohub.domain.AcademicStatus.TITULADO || academic.getStatus() == com.portfoliohub.domain.AcademicStatus.PROFESIONAL)) {
            return label + ". La habilitación profesional es una declaración de la persona usuaria; Portfolio Hub no la verifica.";
        }
        return label;
    }

    private Map<String, Object> education(AcademicRecord academic, Career career) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("university", academic.getUniversityName());
        view.put("faculty", academic.getFacultyId() == null ? null : faculties.findById(academic.getFacultyId()).map(Faculty::getName).orElse(null));
        view.put("career", career != null ? career.getName() : academic.getCareerName());
        view.put("courses", academic.getRelevantCourses());
        view.put("careerEn", career == null ? null : career.getNameEn());
        view.put("status", academic.getStatus());
        view.put("semester", academic.getSemester());
        view.put("admissionYear", academic.getAdmissionYear());
        view.put("expectedGraduation", academic.getExpectedGraduation());
        view.put("interests", academic.getInterests());
        view.put("goals", academic.getProfessionalGoals());
        return view;
    }

    private Map<String, Object> projectView(Project project) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", project.getId());
        view.put("name", project.getName());
        view.put("description", project.getDescription());
        view.put("descriptionEn", project.getDescriptionEn());
        view.put("category", project.getCategory());
        view.put("role", project.getRoleName());
        view.put("results", project.getResults());
        view.put("architectureNote", project.getArchitectureNote());
        view.put("startDate", project.getStartDate());
        view.put("endDate", project.getEndDate());
        view.put("links", project.getLinks());
        view.put("participants", project.getParticipants());
        view.put("coverUrl", publicFile(project.getCoverDocumentId()));
        view.put("technologies", technologies.findByProjectId(project.getId()).stream().map(item -> item.getName()).toList());
        view.put("images", images.findByProjectIdOrderBySortOrderAsc(project.getId()).stream()
                .map(image -> publicFile(image.getDocumentId()))
                .filter(url -> url != null)
                .toList());
        view.put("publicationStatus", project.getPublicationStatus());
        return view;
    }

    private Map<String, Object> experienceView(Experience experience) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", experience.getId());
        view.put("title", experience.getTitle());
        view.put("organization", experience.getOrganization());
        view.put("kind", experience.getKind());
        view.put("description", experience.getDescription());
        view.put("descriptionEn", experience.getDescriptionEn());
        return view;
    }

    private Map<String, Object> courseView(Course course) {
        return Map.of(
                "id", course.getId(),
                "name", course.getName(),
                "institution", course.getInstitution() == null ? "" : course.getInstitution(),
                "status", course.getStatus(),
                "hours", course.getHours() == null ? 0 : course.getHours(),
                "description", course.getDescription() == null ? "" : course.getDescription(),
                "descriptionEn", course.getDescriptionEn() == null ? "" : course.getDescriptionEn()
        );
    }

    private Map<String, Object> certificationView(Certification certification) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", certification.getId());
        view.put("name", certification.getName());
        view.put("issuer", certification.getIssuer());
        view.put("issuedOn", certification.getIssuedOn());
        view.put("description", certification.getDescription());
        view.put("verificationStatus", certification.getVerificationStatus());
        view.put("verificationNote", "Revisado por la persona usuaria. Portfolio Hub no autentica el documento.");
        return view;
    }

    private Map<String, Object> publicationView(Publication publication) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", publication.getId());
        view.put("title", publication.getTitle());
        view.put("authors", publication.getAuthors());
        view.put("venue", publication.getVenue());
        view.put("publishedOn", publication.getPublishedOn());
        view.put("url", publication.getUrl() != null && (publication.getUrl().startsWith("http://") || publication.getUrl().startsWith("https://")) ? publication.getUrl() : null);
        view.put("kind", publication.getKind());
        view.put("summary", publication.getSummary());
        view.put("summaryEn", publication.getSummaryEn());
        return view;
    }

    private List<Map<String, Object>> skillViews(UUID userId, Set<String> hidden) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserSkill link : userSkills.findByUserId(userId)) {
            if (hidden.contains(link.getSkillId().toString())) {
                continue;
            }
            skills.findById(link.getSkillId()).ifPresent(skill -> result.add(Map.of(
                    "id", skill.getId(),
                    "name", skill.getName(),
                    "category", skill.getCategory(),
                    "note", link.getNote() == null ? "" : link.getNote()
            )));
        }
        return result;
    }

    private Map<String, Object> seo(Portfolio portfolio, User user, PersonalProfile personal) {
        String title = portfolio.getSeoTitle() != null ? portfolio.getSeoTitle() : user.getFirstName() + " " + user.getLastName() + " | Portfolio Hub";
        String description = portfolio.getSeoDescription() != null ? portfolio.getSeoDescription() : (personal.getBio() == null ? "Portafolio profesional." : personal.getBio());
        if (description.length() > 180) {
            description = description.substring(0, 177) + "...";
        }
        String canonical = portfolio.getSlug() == null ? properties.getPublicBaseUrl() : properties.getPublicBaseUrl() + "/p/" + portfolio.getSlug();
        ObjectNode jsonLd = mapper.createObjectNode();
        jsonLd.put("@context", "https://schema.org");
        jsonLd.put("@type", "Person");
        jsonLd.put("name", user.getFirstName() + " " + user.getLastName());
        jsonLd.put("description", description);
        jsonLd.put("url", canonical);
        return Map.of("title", title, "description", description, "canonicalUrl", canonical, "jsonLd", jsonLd);
    }

    private String publicFile(UUID documentId) {
        if (documentId == null) {
            return null;
        }
        return documents.findById(documentId)
                .filter(document -> document.getVisibility() == DocumentVisibility.PUBLIC)
                .map(document -> "/api/v1/public/files/" + document.getId())
                .orElse(null);
    }

    private String location(PersonalProfile personal) {
        List<String> parts = new ArrayList<>();
        if (personal.getDistrictId() != null) {
            districts.findById(personal.getDistrictId()).ifPresent(item -> parts.add(item.getName()));
        } else if (personal.getCustomDistrict() != null) {
            parts.add(personal.getCustomDistrict());
        }
        if (personal.getCustomRegion() != null) {
            parts.add(personal.getCustomRegion());
        }
        if (personal.getCustomProvince() != null) {
            parts.add(personal.getCustomProvince());
        }
        if (personal.getCityId() != null) {
            cities.findById(personal.getCityId()).ifPresent(item -> parts.add(item.getName()));
        } else if (personal.getCustomCity() != null) {
            parts.add(personal.getCustomCity());
        }
        if (personal.getCountryId() != null) {
            countries.findById(personal.getCountryId()).ifPresent(item -> parts.add(item.getName()));
        }
        return String.join(", ", parts);
    }

    private List<String> publishGaps(UUID userId) {
        List<String> missing = new ArrayList<>();
        PersonalProfile personal = personalProfiles.findById(userId).orElseThrow();
        if (personal.getBio() == null || personal.getBio().length() < 40) {
            missing.add("Necesitas una biografía de al menos 40 caracteres.");
        }
        AcademicRecord academic = academicRecords.findFirstByUserIdAndPrimaryRecordTrueOrderByUpdatedAtDesc(userId).orElse(null);
        if (academic == null || (academic.getCareerId() == null && (academic.getCareerName() == null || academic.getCareerName().isBlank()))) {
            missing.add("Selecciona tu carrera antes de publicar.");
        }
        boolean hasContent = !projects.findByUserIdOrderByCreatedAtDesc(userId).isEmpty()
                || !courses.findByUserIdOrderByCreatedAtDesc(userId).isEmpty()
                || !certifications.findByUserIdOrderByCreatedAtDesc(userId).isEmpty()
                || !experiences.findByUserIdOrderByStartDateDesc(userId).isEmpty()
                || !userSkills.findByUserId(userId).isEmpty();
        if (!hasContent) {
            missing.add("Añade al menos un proyecto, curso, certificación, experiencia o habilidad.");
        }
        return missing;
    }

    private JsonNode sanitizeTheme(JsonNode incoming, JsonNode previous) {
        ObjectNode theme = previous == null ? mapper.createObjectNode() : previous.deepCopy();
        copyHex(theme, incoming, "accent");
        copyHex(theme, incoming, "accent2");
        copyHex(theme, incoming, "background");
        copyHex(theme, incoming, "surface");
        copyHex(theme, incoming, "text");
        copyHex(theme, incoming, "muted");
        if (incoming.hasNonNull("fontHeading") && FONTS.contains(incoming.get("fontHeading").asText())) {
            theme.put("fontHeading", incoming.get("fontHeading").asText());
        }
        if (incoming.hasNonNull("fontBody") && FONTS.contains(incoming.get("fontBody").asText())) {
            theme.put("fontBody", incoming.get("fontBody").asText());
        }
        if (incoming.hasNonNull("mode") && Set.of("light", "dark").contains(incoming.get("mode").asText())) {
            theme.put("mode", incoming.get("mode").asText());
        }
        if (incoming.hasNonNull("buttonStyle") && Set.of("solid", "outline", "pill", "square").contains(incoming.get("buttonStyle").asText())) {
            theme.put("buttonStyle", incoming.get("buttonStyle").asText());
        }
        if (incoming.hasNonNull("animation") && Set.of("none", "subtle").contains(incoming.get("animation").asText())) {
            theme.put("animation", incoming.get("animation").asText());
        }
        if (incoming.hasNonNull("radius") && Set.of("0", "8", "12", "16", "18", "24").contains(incoming.get("radius").asText())) {
            theme.put("radius", incoming.get("radius").asText());
        }
        if (theme.path("text").asText().equalsIgnoreCase(theme.path("background").asText())) {
            throw ApiException.badRequest("LOW_CONTRAST", "El texto y el fondo no pueden ser del mismo color.");
        }
        return theme;
    }

    private void copyHex(ObjectNode theme, JsonNode incoming, String field) {
        if (incoming.hasNonNull(field) && incoming.get(field).asText().matches("#[0-9A-Fa-f]{6}")) {
            theme.put(field, incoming.get(field).asText());
        }
    }

    private PortfolioVersion snapshot(Portfolio portfolio, String label) {
        if (versions.countByPortfolioId(portfolio.getId()) >= 30) {
            versions.findFirstByPortfolioIdOrderByCreatedAtAsc(portfolio.getId()).ifPresent(versions::delete);
        }
        ArrayNode sectionNodes = mapper.createArrayNode();
        for (PortfolioSection section : sections.findByPortfolioIdOrderBySortOrderAsc(portfolio.getId())) {
            ObjectNode node = mapper.createObjectNode();
            node.put("key", section.getSectionKey());
            node.put("title", section.getTitle());
            node.put("component", section.getComponent());
            node.put("variant", section.getVariant());
            node.put("visible", section.isVisible());
            node.put("sortOrder", section.getSortOrder());
            node.set("content", section.getContent());
            sectionNodes.add(node);
        }
        ObjectNode snap = mapper.createObjectNode();
        if (portfolio.getTemplateId() != null) {
            snap.put("templateId", portfolio.getTemplateId().toString());
        }
        snap.set("theme", portfolio.getTheme());
        snap.put("themeTouched", portfolio.isThemeTouched());
        snap.set("sections", sectionNodes);
        PortfolioVersion version = new PortfolioVersion();
        version.setPortfolioId(portfolio.getId());
        version.setSnapshot(snap);
        version.setLabel(label);
        return versions.save(version);
    }

    private void checkVersion(Portfolio portfolio, long version) {
        if (portfolio.getVersion() != version) {
            throw ApiException.conflict("VERSION_CONFLICT", "El portafolio cambió en otra sesión. Recarga antes de guardar.");
        }
    }

    private void save(Portfolio portfolio) {
        try {
            portfolios.saveAndFlush(portfolio);
        } catch (ObjectOptimisticLockingFailureException exception) {
            throw ApiException.conflict("VERSION_CONFLICT", "El portafolio cambió en otra sesión. Recarga antes de guardar.");
        }
    }

    private Map<String, Object> summary(Portfolio portfolio) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", portfolio.getId());
        view.put("title", portfolio.getTitle());
        view.put("slug", portfolio.getSlug());
        view.put("status", portfolio.getStatus());
        view.put("templateId", portfolio.getTemplateId());
        view.put("careerId", portfolio.getCareerId());
        view.put("theme", portfolio.getTheme());
        view.put("themeTouched", portfolio.isThemeTouched());
        view.put("seoTitle", portfolio.getSeoTitle());
        view.put("seoDescription", portfolio.getSeoDescription());
        view.put("whatsappEnabled", portfolio.isWhatsappEnabled());
        view.put("showEmail", portfolio.isShowEmail());
        view.put("showPhone", portfolio.isShowPhone());
        view.put("bilingual", portfolio.isBilingual());
        view.put("analyticsEnabled", portfolio.isAnalyticsEnabled());
        view.put("recruiterViewEnabled", portfolio.isRecruiterViewEnabled());
        view.put("showcaseOptIn", portfolio.isShowcaseOptIn());
        view.put("consentToPublish", portfolio.isConsentToPublish());
        view.put("publishedAt", portfolio.getPublishedAt());
        view.put("version", portfolio.getVersion());
        view.put("publicUrl", portfolio.getStatus() == PortfolioStatus.PUBLISHED ? publicUrl(portfolio) : null);
        return view;
    }

    public Portfolio owned(UUID userId) {
        return ensure(userId);
    }

    public record SettingsUpdate(
            long version,
            String title,
            JsonNode theme,
            boolean bilingual,
            boolean whatsappEnabled,
            boolean showEmail,
            boolean showPhone,
            boolean analyticsEnabled,
            boolean recruiterViewEnabled,
            boolean showcaseOptIn,
            String seoTitle,
            String seoDescription
    ) {
    }

    public record SectionUpdate(String key, String title, boolean visible, int sortOrder, JsonNode content) {
    }
}
