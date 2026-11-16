package com.portfoliohub.web;

import com.portfoliohub.domain.AcademicStatus;
import com.portfoliohub.security.CurrentUser;
import com.portfoliohub.service.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ProfileController {

    private final ProfileService profiles;

    public ProfileController(ProfileService profiles) {
        this.profiles = profiles;
    }

    @GetMapping("/profiles/me")
    public Map<String, Object> me() {
        return profiles.get(CurrentUser.id());
    }

    @PutMapping("/profiles/me")
    public Map<String, Object> update(@RequestBody ProfileBody body) {
        return profiles.update(CurrentUser.id(), new ProfileService.ProfileUpdate(
                body.firstName(), body.lastName(), body.bio(), body.bioEn(), body.birthDate(), body.showBirthDate(),
                body.countryId(), body.regionId(), body.provinceId(), body.cityId(), body.districtId(),
                body.customRegion(), body.customProvince(), body.customCity(), body.customDistrict(),
                body.address(), body.phone(), body.professionalEmail(), body.linkedin(), body.github(), body.gitlab(),
                body.behance(), body.dribbble(), body.orcid(), body.googleScholar(), body.website(),
                body.otherLinks() == null ? List.of() : body.otherLinks().stream().map(link -> new ProfileService.LinkItem(link.label(), link.url())).toList()
        ));
    }

    @GetMapping("/academic/me")
    public Map<String, Object> academic() {
        return profiles.get(CurrentUser.id());
    }

    @PutMapping("/academic/me")
    public Map<String, Object> updateAcademic(@RequestBody AcademicBody body) {
        return profiles.updateAcademic(CurrentUser.id(), new ProfileService.AcademicUpdate(
                body.universityId(), body.universityName(), body.facultyId(), body.careerId(), body.specializationId(),
                body.semester(), body.admissionYear(), body.expectedGraduation(), body.status(), body.relevantCourses(),
                body.interests(), body.professionalGoals(), body.licenseDeclared(), body.careerName(), body.careerKind()
        ));
    }

    public record ProfileBody(
            String firstName, String lastName, String bio, String bioEn, LocalDate birthDate, boolean showBirthDate,
            UUID countryId, UUID regionId, UUID provinceId, UUID cityId, UUID districtId,
            String customRegion, String customProvince, String customCity, String customDistrict,
            String address, String phone, String professionalEmail, String linkedin, String github, String gitlab,
            String behance, String dribbble, String orcid, String googleScholar, String website, List<LinkBody> otherLinks
    ) {
    }

    public record LinkBody(String label, String url) {
    }

    public record AcademicBody(
            UUID universityId, String universityName, UUID facultyId, UUID careerId, UUID specializationId,
            String semester, Integer admissionYear, LocalDate expectedGraduation, AcademicStatus status,
            String relevantCourses, String interests, String professionalGoals, boolean licenseDeclared,
            String careerName, String careerKind
    ) {
    }
}
