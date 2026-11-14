package com.portfoliohub.service;

import com.portfoliohub.common.ApiException;
import com.portfoliohub.common.Texts;
import com.portfoliohub.domain.AcademicRecord;
import com.portfoliohub.domain.AcademicStatus;
import com.portfoliohub.domain.Career;
import com.portfoliohub.domain.City;
import com.portfoliohub.domain.Country;
import com.portfoliohub.domain.District;
import com.portfoliohub.domain.Faculty;
import com.portfoliohub.domain.PersonalProfile;
import com.portfoliohub.domain.ProfessionalProfile;
import com.portfoliohub.domain.Province;
import com.portfoliohub.domain.Region;
import com.portfoliohub.domain.Specialization;
import com.portfoliohub.domain.University;
import com.portfoliohub.domain.User;
import com.portfoliohub.repo.AcademicRecordRepository;
import com.portfoliohub.repo.CareerRepository;
import com.portfoliohub.repo.CityRepository;
import com.portfoliohub.repo.CountryRepository;
import com.portfoliohub.repo.DistrictRepository;
import com.portfoliohub.repo.FacultyRepository;
import com.portfoliohub.repo.PersonalProfileRepository;
import com.portfoliohub.repo.ProfessionalProfileRepository;
import com.portfoliohub.repo.ProvinceRepository;
import com.portfoliohub.repo.RegionRepository;
import com.portfoliohub.repo.SpecializationRepository;
import com.portfoliohub.repo.UniversityRepository;
import com.portfoliohub.repo.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProfileService {

    private final UserRepository users;
    private final PersonalProfileRepository personalProfiles;
    private final ProfessionalProfileRepository professionalProfiles;
    private final AcademicRecordRepository academicRecords;
    private final CountryRepository countries;
    private final RegionRepository regions;
    private final ProvinceRepository provinces;
    private final CityRepository cities;
    private final DistrictRepository districts;
    private final UniversityRepository universities;
    private final FacultyRepository faculties;
    private final CareerRepository careers;
    private final SpecializationRepository specializations;
    private final PortfolioService portfolios;
    private final ObjectMapper mapper;

    public ProfileService(
            UserRepository users,
            PersonalProfileRepository personalProfiles,
            ProfessionalProfileRepository professionalProfiles,
            AcademicRecordRepository academicRecords,
            CountryRepository countries,
            RegionRepository regions,
            ProvinceRepository provinces,
            CityRepository cities,
            DistrictRepository districts,
            UniversityRepository universities,
            FacultyRepository faculties,
            CareerRepository careers,
            SpecializationRepository specializations,
            PortfolioService portfolios,
            ObjectMapper mapper
    ) {
        this.users = users;
        this.personalProfiles = personalProfiles;
        this.professionalProfiles = professionalProfiles;
        this.academicRecords = academicRecords;
        this.countries = countries;
        this.regions = regions;
        this.provinces = provinces;
        this.cities = cities;
        this.districts = districts;
        this.universities = universities;
        this.faculties = faculties;
        this.careers = careers;
        this.specializations = specializations;
        this.portfolios = portfolios;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> get(UUID userId) {
        User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("No encontramos tu cuenta."));
        PersonalProfile personal = personalProfiles.findById(userId).orElseThrow();
        ProfessionalProfile professional = professionalProfiles.findById(userId).orElseThrow();
        AcademicRecord academic = academicRecords.findFirstByUserIdAndPrimaryRecordTrueOrderByUpdatedAtDesc(userId).orElse(null);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", user.getFirstName());
        body.put("lastName", user.getLastName());
        body.put("email", user.getEmail());
        body.put("onboardingCompleted", user.isOnboardingCompleted());
        body.put("personal", personalView(personal));
        body.put("professional", professionalView(professional));
        body.put("academic", academic == null ? null : academicView(academic));
        return body;
    }

    @Transactional
    public Map<String, Object> update(UUID userId, ProfileUpdate update) {
        User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("No encontramos tu cuenta."));
        if (update.firstName() != null) {
            user.setFirstName(limitName(update.firstName()));
        }
        if (update.lastName() != null) {
            user.setLastName(limitName(update.lastName()));
        }
        PersonalProfile personal = personalProfiles.findById(userId).orElseThrow();
        personal.setBio(Texts.limit(update.bio(), 2000));
        personal.setBioEn(Texts.limit(update.bioEn(), 2000));
        personal.setBirthDate(update.birthDate());
        personal.setShowBirthDate(update.showBirthDate());
        personal.setAddress(Texts.limit(update.address(), 255));
        personal.setPhone(phone(update.phone()));
        personal.setProfessionalEmail(emailOrNull(update.professionalEmail()));
        applyLocation(personal, update);
        ProfessionalProfile professional = professionalProfiles.findById(userId).orElseThrow();
        professional.setLinkedin(Texts.cleanLink(update.linkedin()));
        professional.setGithub(Texts.cleanLink(update.github()));
        professional.setGitlab(Texts.cleanLink(update.gitlab()));
        professional.setBehance(Texts.cleanLink(update.behance()));
        professional.setDribbble(Texts.cleanLink(update.dribbble()));
        professional.setOrcid(Texts.limit(update.orcid(), 40));
        professional.setGoogleScholar(Texts.cleanLink(update.googleScholar()));
        professional.setWebsite(Texts.cleanLink(update.website()));
        professional.setOtherLinks(otherLinks(update.otherLinks()));
        return get(userId);
    }

    @Transactional
    public Map<String, Object> updateAcademic(UUID userId, AcademicUpdate update) {
        AcademicRecord record = academicRecords.findFirstByUserIdAndPrimaryRecordTrueOrderByUpdatedAtDesc(userId).orElseGet(() -> {
            AcademicRecord created = new AcademicRecord();
            created.setUserId(userId);
            created.setPrimaryRecord(true);
            return created;
        });
        if (update.universityId() != null) {
            University university = universities.findById(update.universityId()).filter(University::isActive)
                    .orElseThrow(() -> ApiException.badRequest("INVALID_UNIVERSITY", "Selecciona una universidad del catálogo o escribe su nombre."));
            record.setUniversityId(university.getId());
            record.setUniversityName(university.getName());
        } else {
            record.setUniversityId(null);
            record.setUniversityName(Texts.limit(update.universityName(), 220));
        }
        Faculty faculty = null;
        if (update.facultyId() != null) {
            faculty = faculties.findById(update.facultyId()).filter(Faculty::isActive)
                    .orElseThrow(() -> ApiException.badRequest("INVALID_FACULTY", "Selecciona una facultad válida."));
            record.setFacultyId(faculty.getId());
        } else {
            record.setFacultyId(null);
        }
        if (update.careerId() != null) {
            Career career = careers.findById(update.careerId()).filter(Career::isActive)
                    .orElseThrow(() -> ApiException.badRequest("INVALID_CAREER", "Selecciona una carrera válida."));
            record.setCareerId(career.getId());
        } else {
            record.setCareerId(null);
        }
        if (update.specializationId() != null) {
            Specialization specialization = specializations.findById(update.specializationId())
                    .filter(item -> item.isActive() && item.getCareerId().equals(record.getCareerId()))
                    .orElseThrow(() -> ApiException.badRequest("INVALID_SPECIALIZATION", "La especialización no corresponde a la carrera."));
            record.setSpecializationId(specialization.getId());
        } else {
            record.setSpecializationId(null);
        }
        if (update.status() == null) {
            throw ApiException.badRequest("STATUS_REQUIRED", "Indica tu estado académico.");
        }
        record.setStatus(update.status());
        record.setSemester(Texts.limit(update.semester(), 40));
        if (update.admissionYear() != null && (update.admissionYear() < 1950 || update.admissionYear() > 2100)) {
            throw ApiException.badRequest("INVALID_YEAR", "El año de ingreso no es válido.");
        }
        record.setAdmissionYear(update.admissionYear());
        record.setExpectedGraduation(update.expectedGraduation());
        record.setRelevantCourses(Texts.limit(update.relevantCourses(), 2000));
        record.setInterests(Texts.limit(update.interests(), 2000));
        record.setProfessionalGoals(Texts.limit(update.professionalGoals(), 2000));
        record.setLicenseDeclared(update.licenseDeclared());
        academicRecords.save(record);
        User user = users.findById(userId).orElseThrow();
        if (record.getCareerId() != null && record.getStatus() != null) {
            user.setOnboardingCompleted(true);
        }
        portfolios.syncAcademic(userId);
        return get(userId);
    }

    private void applyLocation(PersonalProfile personal, ProfileUpdate update) {
        personal.setCountryId(optionalCountry(update.countryId()));
        personal.setRegionId(belongs(update.regionId(), personal.getCountryId(), regions.findById(update.regionId() == null ? UUID.randomUUID() : update.regionId()).map(Region::getCountryId).orElse(null), "La región no pertenece al país."));
        if (update.regionId() == null) {
            personal.setRegionId(null);
        }
        personal.setProvinceId(null);
        if (update.provinceId() != null) {
            Province province = provinces.findById(update.provinceId()).orElseThrow(() -> ApiException.badRequest("INVALID_LOCATION", "No encontramos esa provincia."));
            if (personal.getRegionId() != null && !province.getRegionId().equals(personal.getRegionId())) {
                throw ApiException.badRequest("INVALID_LOCATION", "La provincia no pertenece a la región.");
            }
            personal.setProvinceId(province.getId());
        }
        personal.setCityId(null);
        if (update.cityId() != null) {
            City city = cities.findById(update.cityId()).orElseThrow(() -> ApiException.badRequest("INVALID_LOCATION", "No encontramos esa ciudad."));
            if (personal.getCountryId() != null && !city.getCountryId().equals(personal.getCountryId())) {
                throw ApiException.badRequest("INVALID_LOCATION", "La ciudad no pertenece al país.");
            }
            personal.setCityId(city.getId());
        }
        personal.setDistrictId(null);
        if (update.districtId() != null) {
            District district = districts.findById(update.districtId()).orElseThrow(() -> ApiException.badRequest("INVALID_LOCATION", "No encontramos ese distrito."));
            if (personal.getCityId() != null && !district.getCityId().equals(personal.getCityId())) {
                throw ApiException.badRequest("INVALID_LOCATION", "El distrito no pertenece a la ciudad.");
            }
            personal.setDistrictId(district.getId());
        }
        personal.setCustomRegion(Texts.limit(update.customRegion(), 120));
        personal.setCustomProvince(Texts.limit(update.customProvince(), 120));
        personal.setCustomCity(Texts.limit(update.customCity(), 120));
        personal.setCustomDistrict(Texts.limit(update.customDistrict(), 120));
    }

    private UUID optionalCountry(UUID id) {
        if (id == null) {
            return null;
        }
        return countries.findById(id).map(Country::getId).orElseThrow(() -> ApiException.badRequest("INVALID_LOCATION", "No encontramos ese país."));
    }

    private UUID belongs(UUID id, UUID parent, UUID actualParent, String message) {
        if (id == null) {
            return null;
        }
        if (parent != null && actualParent != null && !parent.equals(actualParent)) {
            throw ApiException.badRequest("INVALID_LOCATION", message);
        }
        return id;
    }

    private JsonNode otherLinks(List<LinkItem> links) {
        if (links == null || links.isEmpty()) {
            return null;
        }
        if (links.size() > 8) {
            throw ApiException.badRequest("TOO_MANY_LINKS", "Puedes registrar hasta 8 enlaces adicionales.");
        }
        ArrayNode array = mapper.createArrayNode();
        for (LinkItem link : links) {
            String url = Texts.cleanLink(link.url());
            String label = Texts.limit(link.label(), 40);
            if (url == null || label == null) {
                continue;
            }
            array.add(mapper.createObjectNode().put("label", label).put("url", url));
        }
        return array;
    }

    private Map<String, Object> personalView(PersonalProfile personal) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("photoDocumentId", personal.getPhotoDocumentId());
        view.put("birthDate", personal.getBirthDate());
        view.put("showBirthDate", personal.isShowBirthDate());
        view.put("bio", personal.getBio());
        view.put("bioEn", personal.getBioEn());
        view.put("countryId", personal.getCountryId());
        view.put("regionId", personal.getRegionId());
        view.put("provinceId", personal.getProvinceId());
        view.put("cityId", personal.getCityId());
        view.put("districtId", personal.getDistrictId());
        view.put("customRegion", personal.getCustomRegion());
        view.put("customProvince", personal.getCustomProvince());
        view.put("customCity", personal.getCustomCity());
        view.put("customDistrict", personal.getCustomDistrict());
        view.put("address", personal.getAddress());
        view.put("phone", personal.getPhone());
        view.put("professionalEmail", personal.getProfessionalEmail());
        view.put("locationLabel", locationLabel(personal));
        return view;
    }

    private Map<String, Object> professionalView(ProfessionalProfile professional) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("linkedin", professional.getLinkedin());
        view.put("github", professional.getGithub());
        view.put("gitlab", professional.getGitlab());
        view.put("behance", professional.getBehance());
        view.put("dribbble", professional.getDribbble());
        view.put("orcid", professional.getOrcid());
        view.put("googleScholar", professional.getGoogleScholar());
        view.put("website", professional.getWebsite());
        view.put("otherLinks", professional.getOtherLinks());
        return view;
    }

    private Map<String, Object> academicView(AcademicRecord record) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", record.getId());
        view.put("universityId", record.getUniversityId());
        view.put("universityName", record.getUniversityName());
        view.put("facultyId", record.getFacultyId());
        view.put("careerId", record.getCareerId());
        view.put("specializationId", record.getSpecializationId());
        view.put("semester", record.getSemester());
        view.put("admissionYear", record.getAdmissionYear());
        view.put("expectedGraduation", record.getExpectedGraduation());
        view.put("status", record.getStatus());
        view.put("relevantCourses", record.getRelevantCourses());
        view.put("interests", record.getInterests());
        view.put("professionalGoals", record.getProfessionalGoals());
        view.put("licenseDeclared", record.isLicenseDeclared());
        return view;
    }

    public String locationLabel(PersonalProfile personal) {
        List<String> parts = new ArrayList<>();
        if (personal.getDistrictId() != null) {
            districts.findById(personal.getDistrictId()).ifPresent(item -> parts.add(item.getName()));
        } else if (personal.getCustomDistrict() != null) {
            parts.add(personal.getCustomDistrict());
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

    private String limitName(String value) {
        String trimmed = Texts.blankToNull(value);
        if (trimmed == null || trimmed.length() < 2 || trimmed.length() > 80) {
            throw ApiException.badRequest("INVALID_NAME", "El nombre debe tener entre 2 y 80 caracteres.");
        }
        return trimmed;
    }

    private String phone(String value) {
        String trimmed = Texts.blankToNull(value);
        if (trimmed == null) {
            return null;
        }
        if (!trimmed.matches("[0-9+()\\-\\s]{6,20}")) {
            throw ApiException.badRequest("INVALID_PHONE", "El teléfono solo puede incluir números, espacios y los signos + ( ) -.");
        }
        return trimmed;
    }

    private String emailOrNull(String value) {
        String trimmed = Texts.blankToNull(value);
        if (trimmed == null) {
            return null;
        }
        if (!trimmed.matches("(?i)^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || trimmed.length() > 255) {
            throw ApiException.badRequest("INVALID_EMAIL", "El correo profesional no es válido.");
        }
        return trimmed.toLowerCase();
    }

    public record ProfileUpdate(
            String firstName,
            String lastName,
            String bio,
            String bioEn,
            LocalDate birthDate,
            boolean showBirthDate,
            UUID countryId,
            UUID regionId,
            UUID provinceId,
            UUID cityId,
            UUID districtId,
            String customRegion,
            String customProvince,
            String customCity,
            String customDistrict,
            String address,
            String phone,
            String professionalEmail,
            String linkedin,
            String github,
            String gitlab,
            String behance,
            String dribbble,
            String orcid,
            String googleScholar,
            String website,
            List<LinkItem> otherLinks
    ) {
    }

    public record LinkItem(String label, String url) {
    }

    public record AcademicUpdate(
            UUID universityId,
            String universityName,
            UUID facultyId,
            UUID careerId,
            UUID specializationId,
            String semester,
            Integer admissionYear,
            LocalDate expectedGraduation,
            AcademicStatus status,
            String relevantCourses,
            String interests,
            String professionalGoals,
            boolean licenseDeclared
    ) {
    }
}
