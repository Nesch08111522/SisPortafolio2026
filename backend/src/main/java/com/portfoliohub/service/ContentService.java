package com.portfoliohub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.portfoliohub.common.ApiException;
import com.portfoliohub.common.Texts;
import com.portfoliohub.domain.Certification;
import com.portfoliohub.domain.Course;
import com.portfoliohub.domain.CourseStatus;
import com.portfoliohub.domain.DocumentVisibility;
import com.portfoliohub.domain.Experience;
import com.portfoliohub.domain.ExperienceKind;
import com.portfoliohub.domain.Project;
import com.portfoliohub.domain.ProjectImage;
import com.portfoliohub.domain.ProjectStatus;
import com.portfoliohub.domain.ProjectTechnology;
import com.portfoliohub.domain.Publication;
import com.portfoliohub.domain.PublicationKind;
import com.portfoliohub.domain.Skill;
import com.portfoliohub.domain.SkillCategory;
import com.portfoliohub.domain.StoredDocument;
import com.portfoliohub.domain.UserSkill;
import com.portfoliohub.domain.VerificationStatus;
import com.portfoliohub.repo.CertificationRepository;
import com.portfoliohub.repo.CourseRepository;
import com.portfoliohub.repo.ExperienceRepository;
import com.portfoliohub.repo.ProjectImageRepository;
import com.portfoliohub.repo.ProjectRepository;
import com.portfoliohub.repo.ProjectTechnologyRepository;
import com.portfoliohub.repo.PublicationRepository;
import com.portfoliohub.repo.SkillRepository;
import com.portfoliohub.repo.StoredDocumentRepository;
import com.portfoliohub.repo.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ContentService {

    private final ProjectRepository projects;
    private final ProjectTechnologyRepository technologies;
    private final ProjectImageRepository images;
    private final CourseRepository courses;
    private final CertificationRepository certifications;
    private final ExperienceRepository experiences;
    private final PublicationRepository publications;
    private final SkillRepository skills;
    private final UserSkillRepository userSkills;
    private final StoredDocumentRepository documents;
    private final ObjectMapper mapper;

    public ContentService(
            ProjectRepository projects,
            ProjectTechnologyRepository technologies,
            ProjectImageRepository images,
            CourseRepository courses,
            CertificationRepository certifications,
            ExperienceRepository experiences,
            PublicationRepository publications,
            SkillRepository skills,
            UserSkillRepository userSkills,
            StoredDocumentRepository documents,
            ObjectMapper mapper
    ) {
        this.projects = projects;
        this.technologies = technologies;
        this.images = images;
        this.courses = courses;
        this.certifications = certifications;
        this.experiences = experiences;
        this.publications = publications;
        this.skills = skills;
        this.userSkills = userSkills;
        this.documents = documents;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<Project> projects(UUID userId) {
        return projects.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Project saveProject(UUID userId, UUID id, ProjectInput input) {
        Project project = id == null ? new Project() : projects.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("No encontramos ese proyecto."));
        project.setUserId(userId);
        String name = Texts.limit(input.name(), 200);
        if (name == null) {
            throw ApiException.badRequest("NAME_REQUIRED", "El proyecto necesita un nombre.");
        }
        project.setName(name);
        project.setDescription(Texts.limit(input.description(), 4000));
        project.setDescriptionEn(Texts.limit(input.descriptionEn(), 4000));
        project.setCategory(Texts.limit(input.category(), 80));
        project.setRoleName(Texts.limit(input.roleName(), 120));
        project.setResults(Texts.limit(input.results(), 2000));
        project.setArchitectureNote(Texts.limit(input.architectureNote(), 2000));
        project.setStartDate(input.startDate());
        project.setEndDate(input.endDate());
        project.setPublicationStatus(input.publicationStatus() == null ? ProjectStatus.DRAFT : input.publicationStatus());
        project.setCoverDocumentId(ownedDocument(userId, input.coverDocumentId()));
        project.setLinks(links(input.links()));
        project.setParticipants(participants(input.participants()));
        projects.save(project);
        technologies.deleteByProjectId(project.getId());
        if (input.technologies() != null) {
            int count = 0;
            for (String technology : input.technologies()) {
                String clean = Texts.limit(technology, 80);
                if (clean == null || count++ >= 20) {
                    continue;
                }
                ProjectTechnology item = new ProjectTechnology();
                item.setProjectId(project.getId());
                item.setName(clean);
                technologies.save(item);
            }
        }
        return project;
    }

    @Transactional
    public void deleteProject(UUID userId, UUID id) {
        Project project = projects.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("No encontramos ese proyecto."));
        project.setDeletedAt(Instant.now());
    }

    @Transactional
    public void addProjectImage(UUID userId, UUID projectId, UUID documentId) {
        Project project = projects.findByIdAndUserId(projectId, userId).orElseThrow(() -> ApiException.notFound("No encontramos ese proyecto."));
        ProjectImage image = new ProjectImage();
        image.setProjectId(project.getId());
        image.setDocumentId(ownedDocument(userId, documentId));
        image.setSortOrder(images.findByProjectIdOrderBySortOrderAsc(project.getId()).size());
        images.save(image);
    }

    @Transactional(readOnly = true)
    public List<Course> courses(UUID userId) {
        return courses.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Course saveCourse(UUID userId, UUID id, CourseInput input) {
        Course course = id == null ? new Course() : courses.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("No encontramos ese curso."));
        course.setUserId(userId);
        String name = Texts.limit(input.name(), 200);
        if (name == null) {
            throw ApiException.badRequest("NAME_REQUIRED", "El curso necesita un nombre.");
        }
        course.setName(name);
        course.setInstitution(Texts.limit(input.institution(), 200));
        course.setStartDate(input.startDate());
        course.setEndDate(input.endDate());
        if (input.hours() != null && input.hours() < 0) {
            throw ApiException.badRequest("INVALID_HOURS", "Las horas no pueden ser negativas.");
        }
        course.setHours(input.hours());
        course.setStatus(input.status() == null ? CourseStatus.COMPLETADO : input.status());
        course.setDescription(Texts.limit(input.description(), 2000));
        course.setDescriptionEn(Texts.limit(input.descriptionEn(), 2000));
        course.setCertificateDocumentId(ownedDocument(userId, input.certificateDocumentId()));
        return courses.save(course);
    }

    @Transactional
    public void deleteCourse(UUID userId, UUID id) {
        Course course = courses.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("No encontramos ese curso."));
        course.setDeletedAt(Instant.now());
    }

    @Transactional(readOnly = true)
    public List<Certification> certifications(UUID userId) {
        return certifications.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Certification saveCertification(UUID userId, UUID id, CertificationInput input) {
        Certification certification = id == null ? new Certification() : certifications.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("No encontramos esa certificación."));
        certification.setUserId(userId);
        String name = Texts.limit(input.name(), 200);
        if (name == null) {
            throw ApiException.badRequest("NAME_REQUIRED", "La certificación necesita un nombre.");
        }
        certification.setName(name);
        certification.setIssuer(Texts.limit(input.issuer(), 200));
        certification.setIssuedOn(input.issuedOn());
        certification.setExpiresOn(input.expiresOn());
        certification.setCredentialId(Texts.limit(input.credentialId(), 120));
        certification.setDescription(Texts.limit(input.description(), 2000));
        certification.setDescriptionEn(Texts.limit(input.descriptionEn(), 2000));
        certification.setDocumentId(ownedDocument(userId, input.documentId()));
        certification.setVerificationStatus(VerificationStatus.USER_CONFIRMED);
        return certifications.save(certification);
    }

    @Transactional
    public void deleteCertification(UUID userId, UUID id) {
        Certification certification = certifications.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("No encontramos esa certificación."));
        certification.setDeletedAt(Instant.now());
    }

    @Transactional(readOnly = true)
    public List<Experience> experiences(UUID userId) {
        return experiences.findByUserIdOrderByStartDateDesc(userId);
    }

    @Transactional
    public Experience saveExperience(UUID userId, UUID id, ExperienceInput input) {
        Experience experience = id == null ? new Experience() : experiences.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("No encontramos esa experiencia."));
        experience.setUserId(userId);
        String title = Texts.limit(input.title(), 200);
        if (title == null || input.kind() == null) {
            throw ApiException.badRequest("INVALID_EXPERIENCE", "Indica el título y el tipo de experiencia.");
        }
        experience.setTitle(title);
        experience.setOrganization(Texts.limit(input.organization(), 200));
        experience.setKind(input.kind());
        experience.setStartDate(input.startDate());
        experience.setEndDate(input.endDate());
        experience.setDescription(Texts.limit(input.description(), 2000));
        experience.setDescriptionEn(Texts.limit(input.descriptionEn(), 2000));
        return experiences.save(experience);
    }

    @Transactional
    public void deleteExperience(UUID userId, UUID id) {
        Experience experience = experiences.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("No encontramos esa experiencia."));
        experience.setDeletedAt(Instant.now());
    }

    @Transactional(readOnly = true)
    public List<Publication> publications(UUID userId) {
        return publications.findByUserIdOrderByPublishedOnDesc(userId);
    }

    @Transactional
    public Publication savePublication(UUID userId, UUID id, PublicationInput input) {
        Publication publication = id == null ? new Publication() : publications.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("No encontramos esa publicación."));
        publication.setUserId(userId);
        String title = Texts.limit(input.title(), 255);
        if (title == null || input.kind() == null) {
            throw ApiException.badRequest("INVALID_PUBLICATION", "Indica el título y el tipo de publicación.");
        }
        publication.setTitle(title);
        publication.setAuthors(Texts.limit(input.authors(), 255));
        publication.setVenue(Texts.limit(input.venue(), 255));
        publication.setPublishedOn(input.publishedOn());
        publication.setUrl(Texts.cleanLink(input.url()));
        publication.setKind(input.kind());
        publication.setSummary(Texts.limit(input.summary(), 2000));
        publication.setSummaryEn(Texts.limit(input.summaryEn(), 2000));
        return publications.save(publication);
    }

    @Transactional
    public void deletePublication(UUID userId, UUID id) {
        Publication publication = publications.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("No encontramos esa publicación."));
        publication.setDeletedAt(Instant.now());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> skills(UUID userId) {
        return userSkills.findByUserId(userId).stream().map(link -> {
            Skill skill = skills.findById(link.getSkillId()).orElseThrow();
            return Map.<String, Object>of(
                    "id", skill.getId(),
                    "name", skill.getName(),
                    "category", skill.getCategory(),
                    "note", link.getNote() == null ? "" : link.getNote()
            );
        }).toList();
    }

    @Transactional
    public Map<String, Object> addSkill(UUID userId, String name, SkillCategory category, String note) {
        String clean = Texts.limit(name, 120);
        if (clean == null || category == null) {
            throw ApiException.badRequest("INVALID_SKILL", "Indica el nombre y la categoría de la habilidad.");
        }
        Skill skill = skills.findByNameIgnoreCaseAndCategory(clean, category).orElseGet(() -> {
            Skill created = new Skill();
            created.setName(clean);
            created.setCategory(category);
            created.setCreatedBy(userId);
            return skills.save(created);
        });
        UserSkill link = new UserSkill();
        link.setUserId(userId);
        link.setSkillId(skill.getId());
        link.setNote(Texts.limit(note, 255));
        userSkills.save(link);
        return Map.of("id", skill.getId(), "name", skill.getName(), "category", skill.getCategory(), "note", link.getNote() == null ? "" : link.getNote());
    }

    @Transactional
    public void removeSkill(UUID userId, UUID skillId) {
        userSkills.deleteByUserIdAndSkillId(userId, skillId);
    }

    private UUID ownedDocument(UUID userId, UUID documentId) {
        if (documentId == null) {
            return null;
        }
        StoredDocument document = documents.findByIdAndUserId(documentId, userId).orElseThrow(() -> ApiException.notFound("No encontramos ese documento."));
        return document.getId();
    }

    private com.fasterxml.jackson.databind.JsonNode links(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        ObjectNode node = mapper.createObjectNode();
        values.forEach((key, value) -> {
            if (key != null && key.matches("[a-zA-Z]{2,20}")) {
                String clean = Texts.cleanLink(value);
                if (clean != null) {
                    node.put(key, clean);
                }
            }
        });
        return node.size() == 0 ? null : node;
    }

    private com.fasterxml.jackson.databind.JsonNode participants(List<Participant> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        ArrayNode array = mapper.createArrayNode();
        int count = 0;
        for (Participant participant : values) {
            if (count++ >= 12) {
                break;
            }
            String name = Texts.limit(participant.name(), 80);
            if (name == null) {
                continue;
            }
            array.add(mapper.createObjectNode().put("name", name).put("role", Texts.limit(participant.role(), 80) == null ? "" : Texts.limit(participant.role(), 80)));
        }
        return array;
    }

    public void ensurePublicImage(UUID userId, UUID documentId) {
        if (documentId == null) {
            return;
        }
        StoredDocument document = documents.findByIdAndUserId(documentId, userId).orElseThrow(() -> ApiException.notFound("No encontramos ese documento."));
        if (document.getVisibility() != DocumentVisibility.PUBLIC && !document.getMimeType().startsWith("image/")) {
            throw ApiException.badRequest("PRIVATE_FILE", "El archivo no está marcado como visible.");
        }
    }

    public record ProjectInput(
            String name, String description, String descriptionEn, String category, LocalDate startDate, LocalDate endDate,
            String roleName, String results, String architectureNote, Map<String, String> links, List<Participant> participants,
            List<String> technologies, ProjectStatus publicationStatus, UUID coverDocumentId
    ) {
    }

    public record Participant(String name, String role) {
    }

    public record CourseInput(
            String name, String institution, LocalDate startDate, LocalDate endDate, Integer hours, CourseStatus status,
            String description, String descriptionEn, UUID certificateDocumentId
    ) {
    }

    public record CertificationInput(
            String name, String issuer, LocalDate issuedOn, LocalDate expiresOn, String credentialId,
            String description, String descriptionEn, UUID documentId
    ) {
    }

    public record ExperienceInput(
            String title, String organization, ExperienceKind kind, LocalDate startDate, LocalDate endDate,
            String description, String descriptionEn
    ) {
    }

    public record PublicationInput(
            String title, String authors, String venue, LocalDate publishedOn, String url, PublicationKind kind,
            String summary, String summaryEn
    ) {
    }
}
