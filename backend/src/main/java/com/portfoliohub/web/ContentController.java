package com.portfoliohub.web;

import com.portfoliohub.domain.CourseStatus;
import com.portfoliohub.domain.ExperienceKind;
import com.portfoliohub.domain.ProjectStatus;
import com.portfoliohub.domain.PublicationKind;
import com.portfoliohub.domain.SkillCategory;
import com.portfoliohub.security.CurrentUser;
import com.portfoliohub.service.ContentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ContentController {

    private final ContentService content;

    public ContentController(ContentService content) {
        this.content = content;
    }

    @GetMapping("/projects")
    public List<?> projects() {
        return content.projects(CurrentUser.id());
    }

    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public Object createProject(@RequestBody ProjectBody body) {
        return content.saveProject(CurrentUser.id(), null, toProject(body));
    }

    @PutMapping("/projects/{id}")
    public Object updateProject(@PathVariable UUID id, @RequestBody ProjectBody body) {
        return content.saveProject(CurrentUser.id(), id, toProject(body));
    }

    @DeleteMapping("/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable UUID id) {
        content.deleteProject(CurrentUser.id(), id);
    }

    @PostMapping("/projects/{id}/images")
    public void addImage(@PathVariable UUID id, @RequestBody Map<String, UUID> body) {
        content.addProjectImage(CurrentUser.id(), id, body.get("documentId"));
    }

    @GetMapping("/courses")
    public List<?> courses() {
        return content.courses(CurrentUser.id());
    }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public Object createCourse(@RequestBody CourseBody body) {
        return content.saveCourse(CurrentUser.id(), null, toCourse(body));
    }

    @PutMapping("/courses/{id}")
    public Object updateCourse(@PathVariable UUID id, @RequestBody CourseBody body) {
        return content.saveCourse(CurrentUser.id(), id, toCourse(body));
    }

    @DeleteMapping("/courses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(@PathVariable UUID id) {
        content.deleteCourse(CurrentUser.id(), id);
    }

    @GetMapping("/certifications")
    public List<?> certifications() {
        return content.certifications(CurrentUser.id());
    }

    @PostMapping("/certifications")
    @ResponseStatus(HttpStatus.CREATED)
    public Object createCertification(@RequestBody CertificationBody body) {
        return content.saveCertification(CurrentUser.id(), null, toCertification(body));
    }

    @PutMapping("/certifications/{id}")
    public Object updateCertification(@PathVariable UUID id, @RequestBody CertificationBody body) {
        return content.saveCertification(CurrentUser.id(), id, toCertification(body));
    }

    @DeleteMapping("/certifications/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCertification(@PathVariable UUID id) {
        content.deleteCertification(CurrentUser.id(), id);
    }

    @GetMapping("/experiences")
    public List<?> experiences() {
        return content.experiences(CurrentUser.id());
    }

    @PostMapping("/experiences")
    @ResponseStatus(HttpStatus.CREATED)
    public Object createExperience(@RequestBody ExperienceBody body) {
        return content.saveExperience(CurrentUser.id(), null, toExperience(body));
    }

    @PutMapping("/experiences/{id}")
    public Object updateExperience(@PathVariable UUID id, @RequestBody ExperienceBody body) {
        return content.saveExperience(CurrentUser.id(), id, toExperience(body));
    }

    @DeleteMapping("/experiences/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExperience(@PathVariable UUID id) {
        content.deleteExperience(CurrentUser.id(), id);
    }

    @GetMapping("/publications")
    public List<?> publications() {
        return content.publications(CurrentUser.id());
    }

    @PostMapping("/publications")
    @ResponseStatus(HttpStatus.CREATED)
    public Object createPublication(@RequestBody PublicationBody body) {
        return content.savePublication(CurrentUser.id(), null, toPublication(body));
    }

    @PutMapping("/publications/{id}")
    public Object updatePublication(@PathVariable UUID id, @RequestBody PublicationBody body) {
        return content.savePublication(CurrentUser.id(), id, toPublication(body));
    }

    @DeleteMapping("/publications/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePublication(@PathVariable UUID id) {
        content.deletePublication(CurrentUser.id(), id);
    }

    @GetMapping("/skills")
    public List<Map<String, Object>> skills() {
        return content.skills(CurrentUser.id());
    }

    @PostMapping("/skills")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addSkill(@RequestBody SkillBody body) {
        return content.addSkill(CurrentUser.id(), body.name(), body.category(), body.note());
    }

    @DeleteMapping("/skills/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSkill(@PathVariable UUID id) {
        content.removeSkill(CurrentUser.id(), id);
    }

    private ContentService.ProjectInput toProject(ProjectBody body) {
        return new ContentService.ProjectInput(
                body.name(), body.description(), body.descriptionEn(), body.category(), body.startDate(), body.endDate(),
                body.roleName(), body.results(), body.architectureNote(), body.links(),
                body.participants() == null ? List.of() : body.participants().stream().map(item -> new ContentService.Participant(item.name(), item.role())).toList(),
                body.technologies(), body.publicationStatus(), body.coverDocumentId()
        );
    }

    private ContentService.CourseInput toCourse(CourseBody body) {
        return new ContentService.CourseInput(body.name(), body.institution(), body.startDate(), body.endDate(), body.hours(), body.status(), body.description(), body.descriptionEn(), body.certificateDocumentId());
    }

    private ContentService.CertificationInput toCertification(CertificationBody body) {
        return new ContentService.CertificationInput(body.name(), body.issuer(), body.issuedOn(), body.expiresOn(), body.credentialId(), body.description(), body.descriptionEn(), body.documentId());
    }

    private ContentService.ExperienceInput toExperience(ExperienceBody body) {
        return new ContentService.ExperienceInput(body.title(), body.organization(), body.kind(), body.startDate(), body.endDate(), body.description(), body.descriptionEn());
    }

    private ContentService.PublicationInput toPublication(PublicationBody body) {
        return new ContentService.PublicationInput(body.title(), body.authors(), body.venue(), body.publishedOn(), body.url(), body.kind(), body.summary(), body.summaryEn());
    }

    public record ProjectBody(
            String name, String description, String descriptionEn, String category, LocalDate startDate, LocalDate endDate,
            String roleName, String results, String architectureNote, Map<String, String> links, List<ParticipantBody> participants,
            List<String> technologies, ProjectStatus publicationStatus, UUID coverDocumentId
    ) {
    }

    public record ParticipantBody(String name, String role) {
    }

    public record CourseBody(
            String name, String institution, LocalDate startDate, LocalDate endDate, Integer hours, CourseStatus status,
            String description, String descriptionEn, UUID certificateDocumentId
    ) {
    }

    public record CertificationBody(
            String name, String issuer, LocalDate issuedOn, LocalDate expiresOn, String credentialId,
            String description, String descriptionEn, UUID documentId
    ) {
    }

    public record ExperienceBody(
            String title, String organization, ExperienceKind kind, LocalDate startDate, LocalDate endDate,
            String description, String descriptionEn
    ) {
    }

    public record PublicationBody(
            String title, String authors, String venue, LocalDate publishedOn, String url, PublicationKind kind,
            String summary, String summaryEn
    ) {
    }

    public record SkillBody(String name, SkillCategory category, String note) {
    }
}
