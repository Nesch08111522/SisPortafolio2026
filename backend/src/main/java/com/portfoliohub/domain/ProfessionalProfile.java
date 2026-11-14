package com.portfoliohub.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "professional_profiles")
@Getter
@Setter
public class ProfessionalProfile extends Timestamps {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(length = 255)
    private String linkedin;

    @Column(length = 255)
    private String github;

    @Column(length = 255)
    private String gitlab;

    @Column(length = 255)
    private String behance;

    @Column(length = 255)
    private String dribbble;

    @Column(length = 40)
    private String orcid;

    @Column(name = "google_scholar", length = 255)
    private String googleScholar;

    @Column(length = 255)
    private String website;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "other_links", columnDefinition = "jsonb")
    private JsonNode otherLinks;
}
