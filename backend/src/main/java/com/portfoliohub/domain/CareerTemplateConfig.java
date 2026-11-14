package com.portfoliohub.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "career_template_configs")
@Getter
@Setter
public class CareerTemplateConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "career_id")
    private UUID careerId;

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "variant_name", nullable = false, length = 160)
    private String variantName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "section_schema", nullable = false, columnDefinition = "jsonb")
    private JsonNode sectionSchema;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "theme_overrides", nullable = false, columnDefinition = "jsonb")
    private JsonNode themeOverrides;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode competencies;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_examples", nullable = false, columnDefinition = "jsonb")
    private JsonNode contentExamples;

    @Column(columnDefinition = "text")
    private String disclaimer;

    @Column(name = "student_safe", nullable = false)
    private boolean studentSafe;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean generated = true;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }
}
