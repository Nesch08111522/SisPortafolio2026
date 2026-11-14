package com.portfoliohub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "academic_records")
@Getter
@Setter
public class AcademicRecord extends Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "university_id")
    private UUID universityId;

    @Column(name = "university_name", length = 220)
    private String universityName;

    @Column(name = "faculty_id")
    private UUID facultyId;

    @Column(name = "career_id")
    private UUID careerId;

    @Column(name = "specialization_id")
    private UUID specializationId;

    @Column(length = 40)
    private String semester;

    @Column(name = "admission_year")
    private Integer admissionYear;

    @Column(name = "expected_graduation")
    private LocalDate expectedGraduation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AcademicStatus status;

    @Column(name = "relevant_courses", columnDefinition = "text")
    private String relevantCourses;

    @Column(columnDefinition = "text")
    private String interests;

    @Column(name = "professional_goals", columnDefinition = "text")
    private String professionalGoals;

    @Column(name = "license_declared", nullable = false)
    private boolean licenseDeclared;

    @Column(name = "primary_record", nullable = false)
    private boolean primaryRecord = true;
}
