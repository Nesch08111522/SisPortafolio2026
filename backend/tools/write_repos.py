from pathlib import Path

root = Path(__file__).resolve().parents[1] / "src" / "main" / "java" / "com" / "portfoliohub" / "repo"
root.mkdir(parents=True, exist_ok=True)

files = {
"UserRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
""",
"RoleRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);
}
""",
"VerificationTokenRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.TokenType;
import com.portfoliohub.domain.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByTokenHashAndType(String tokenHash, TokenType type);
    void deleteByUserIdAndType(UUID userId, TokenType type);
}
""",
"RefreshTokenRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void deleteByUserId(UUID userId);
}
""",
"OutboundMessageRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.OutboundMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboundMessageRepository extends JpaRepository<OutboundMessage, UUID> {
    List<OutboundMessage> findTop20ByRecipientIgnoreCaseOrderByCreatedAtDesc(String recipient);
}
""",
"CountryRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CountryRepository extends JpaRepository<Country, UUID> {
    List<Country> findByActiveTrueOrderByNameAsc();
    Optional<Country> findByIso2IgnoreCase(String iso2);
}
""",
"RegionRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegionRepository extends JpaRepository<Region, UUID> {
    List<Region> findByCountryIdAndActiveTrueOrderByNameAsc(UUID countryId);
    Optional<Region> findByCountryIdAndNameIgnoreCase(UUID countryId, String name);
}
""",
"ProvinceRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.Province;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProvinceRepository extends JpaRepository<Province, UUID> {
    List<Province> findByRegionIdAndActiveTrueOrderByNameAsc(UUID regionId);
}
""",
"CityRepository.java": """
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
""",
"DistrictRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DistrictRepository extends JpaRepository<District, UUID> {
    List<District> findByCityIdAndActiveTrueOrderByNameAsc(UUID cityId);
}
""",
"UniversityRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.University;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UniversityRepository extends JpaRepository<University, UUID> {
    Page<University> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
    Page<University> findByActiveTrue(Pageable pageable);
}
""",
"FacultyRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FacultyRepository extends JpaRepository<Faculty, UUID> {
    List<Faculty> findByActiveTrueOrderByNameAsc();
    Optional<Faculty> findByCode(String code);
}
""",
"CareerRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.Career;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CareerRepository extends JpaRepository<Career, UUID> {
    Optional<Career> findByCode(String code);

    @Query("select distinct c from Career c left join c.faculties f where c.active = true and (:facultyId is null or f.id = :facultyId) and (:q is null or :q = '' or lower(c.name) like lower(concat('%', :q, '%')) or lower(coalesce(c.nameEn, '')) like lower(concat('%', :q, '%')) or lower(c.code) like lower(concat('%', :q, '%')))")
    Page<Career> search(UUID facultyId, String q, Pageable pageable);
}
""",
"SpecializationRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpecializationRepository extends JpaRepository<Specialization, UUID> {
    List<Specialization> findByCareerIdAndActiveTrueOrderByNameAsc(UUID careerId);
    List<Specialization> findByCareerIdOrderByNameAsc(UUID careerId);
}
""",
"CareerSynonymRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.CareerSynonym;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CareerSynonymRepository extends JpaRepository<CareerSynonym, UUID> {
    List<CareerSynonym> findByCareerId(UUID careerId);
}
""",
"AcademicRecordRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.AcademicRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, UUID> {
    Optional<AcademicRecord> findFirstByUserIdAndPrimaryRecordTrueOrderByUpdatedAtDesc(UUID userId);
}
""",
"PersonalProfileRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.PersonalProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PersonalProfileRepository extends JpaRepository<PersonalProfile, UUID> {
}
""",
"ProfessionalProfileRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.ProfessionalProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfessionalProfileRepository extends JpaRepository<ProfessionalProfile, UUID> {
}
""",
"StoredDocumentRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.StoredDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoredDocumentRepository extends JpaRepository<StoredDocument, UUID> {
    List<StoredDocument> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<StoredDocument> findByIdAndUserId(UUID id, UUID userId);
}
""",
"DocumentExtractionRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.DocumentExtraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentExtractionRepository extends JpaRepository<DocumentExtraction, UUID> {
    Optional<DocumentExtraction> findByIdAndDocumentId(UUID id, UUID documentId);
    List<DocumentExtraction> findByDocumentIdOrderByCreatedAtDesc(UUID documentId);
}
""",
"SkillRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.Skill;
import com.portfoliohub.domain.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {
    Optional<Skill> findByNameIgnoreCaseAndCategory(String name, SkillCategory category);
}
""",
"UserSkillRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserSkillRepository extends JpaRepository<UserSkill, UserSkill.Key> {
    List<UserSkill> findByUserId(UUID userId);
    void deleteByUserIdAndSkillId(UUID userId, UUID skillId);
}
""",
"CourseRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Course> findByIdAndUserId(UUID id, UUID userId);
}
""",
"CertificationRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CertificationRepository extends JpaRepository<Certification, UUID> {
    List<Certification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Certification> findByIdAndUserId(UUID id, UUID userId);
}
""",
"ProjectRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Project> findByIdAndUserId(UUID id, UUID userId);
}
""",
"ProjectImageRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.ProjectImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectImageRepository extends JpaRepository<ProjectImage, UUID> {
    List<ProjectImage> findByProjectIdOrderBySortOrderAsc(UUID projectId);
    void deleteByProjectId(UUID projectId);
}
""",
"ProjectTechnologyRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.ProjectTechnology;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectTechnologyRepository extends JpaRepository<ProjectTechnology, UUID> {
    List<ProjectTechnology> findByProjectId(UUID projectId);
    void deleteByProjectId(UUID projectId);
}
""",
"ExperienceRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.Experience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExperienceRepository extends JpaRepository<Experience, UUID> {
    List<Experience> findByUserIdOrderByStartDateDesc(UUID userId);
    Optional<Experience> findByIdAndUserId(UUID id, UUID userId);
}
""",
"PublicationRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.Publication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PublicationRepository extends JpaRepository<Publication, UUID> {
    List<Publication> findByUserIdOrderByPublishedOnDesc(UUID userId);
    Optional<Publication> findByIdAndUserId(UUID id, UUID userId);
}
""",
"TemplateDefinitionRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.TemplateDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateDefinitionRepository extends JpaRepository<TemplateDefinition, UUID> {
    List<TemplateDefinition> findByActiveTrueOrderBySortOrderAsc();
    Optional<TemplateDefinition> findByCode(String code);
}
""",
"CareerTemplateConfigRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.CareerTemplateConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CareerTemplateConfigRepository extends JpaRepository<CareerTemplateConfig, UUID> {
    List<CareerTemplateConfig> findByCareerIdAndActiveTrueOrderBySortOrderAsc(UUID careerId);
    List<CareerTemplateConfig> findByCareerIdIsNullAndActiveTrueOrderBySortOrderAsc();
    Optional<CareerTemplateConfig> findByCareerIdAndTemplateId(UUID careerId, UUID templateId);
    Optional<CareerTemplateConfig> findByCareerIdIsNullAndTemplateId(UUID templateId);
    boolean existsByCareerId(UUID careerId);
    boolean existsByCareerIdIsNull();
}
""",
"PortfolioRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.Portfolio;
import com.portfoliohub.domain.PortfolioStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {
    Optional<Portfolio> findByUserId(UUID userId);
    Optional<Portfolio> findBySlugIgnoreCase(String slug);
    boolean existsBySlugIgnoreCase(String slug);
    Page<Portfolio> findByStatusAndShowcaseOptInTrue(PortfolioStatus status, Pageable pageable);
}
""",
"PortfolioSectionRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.PortfolioSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioSectionRepository extends JpaRepository<PortfolioSection, UUID> {
    List<PortfolioSection> findByPortfolioIdOrderBySortOrderAsc(UUID portfolioId);
    Optional<PortfolioSection> findByPortfolioIdAndSectionKey(UUID portfolioId, String sectionKey);
    void deleteByPortfolioIdAndSectionKey(UUID portfolioId, String sectionKey);
}
""",
"PortfolioVersionRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.PortfolioVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PortfolioVersionRepository extends JpaRepository<PortfolioVersion, UUID> {
    List<PortfolioVersion> findTop30ByPortfolioIdOrderByCreatedAtDesc(UUID portfolioId);
    long countByPortfolioId(UUID portfolioId);
}
""",
"PortfolioPublicationRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.PortfolioPublication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PortfolioPublicationRepository extends JpaRepository<PortfolioPublication, UUID> {
    List<PortfolioPublication> findByPortfolioIdOrderByPublishedAtDesc(UUID portfolioId);
}
""",
"PortfolioDomainRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.PortfolioDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PortfolioDomainRepository extends JpaRepository<PortfolioDomain, UUID> {
    List<PortfolioDomain> findByPortfolioIdOrderByCreatedAtDesc(UUID portfolioId);
}
""",
"PortfolioVisitRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.PortfolioVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PortfolioVisitRepository extends JpaRepository<PortfolioVisit, UUID> {
    long countByPortfolioId(UUID portfolioId);

    @Query("select v.device, count(v) from PortfolioVisit v where v.portfolioId = :portfolioId and v.visitedAt >= :since group by v.device")
    List<Object[]> countByDevice(UUID portfolioId, Instant since);

    List<PortfolioVisit> findByPortfolioIdAndVisitedAtAfterOrderByVisitedAtDesc(UUID portfolioId, Instant since);
}
""",
"ContactMessageRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, UUID> {
    List<ContactMessage> findByPortfolioIdOrderByCreatedAtDesc(UUID portfolioId);
}
""",
"NotificationRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findTop50ByUserIdOrderByCreatedAtDesc(UUID userId);
    long countByUserIdAndReadAtIsNull(UUID userId);
}
""",
"AuditLogRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
""",
"GeoCacheEntryRepository.java": """
package com.portfoliohub.repo;

import com.portfoliohub.domain.GeoCacheEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GeoCacheEntryRepository extends JpaRepository<GeoCacheEntry, UUID> {
    Optional<GeoCacheEntry> findByProviderAndQueryKey(String provider, String queryKey);
}
""",
}

for name, content in files.items():
    (root / name).write_text(content.strip() + "\n", encoding="utf-8")
print(f"wrote {len(files)} repositories")
