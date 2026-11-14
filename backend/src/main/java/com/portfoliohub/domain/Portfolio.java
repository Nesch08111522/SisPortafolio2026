package com.portfoliohub.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "portfolios")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Portfolio extends Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 80)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PortfolioStatus status = PortfolioStatus.DRAFT;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "career_id")
    private UUID careerId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode theme;

    @Column(name = "theme_touched", nullable = false)
    private boolean themeTouched;

    @Column(name = "seo_title", length = 160)
    private String seoTitle;

    @Column(name = "seo_description", length = 300)
    private String seoDescription;

    @Column(name = "whatsapp_enabled", nullable = false)
    private boolean whatsappEnabled;

    @Column(name = "show_email", nullable = false)
    private boolean showEmail;

    @Column(name = "show_phone", nullable = false)
    private boolean showPhone;

    @Column(nullable = false)
    private boolean bilingual;

    @Column(name = "analytics_enabled", nullable = false)
    private boolean analyticsEnabled;

    @Column(name = "recruiter_view_enabled", nullable = false)
    private boolean recruiterViewEnabled = true;

    @Column(name = "showcase_opt_in", nullable = false)
    private boolean showcaseOptIn;

    @Column(name = "consent_to_publish", nullable = false)
    private boolean consentToPublish;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
