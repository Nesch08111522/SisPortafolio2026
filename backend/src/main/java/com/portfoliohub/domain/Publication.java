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
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "publications")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Publication extends Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String authors;

    @Column(length = 255)
    private String venue;

    @Column(name = "published_on")
    private LocalDate publishedOn;

    @Column(length = 255)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PublicationKind kind;

    @Column(columnDefinition = "text")
    private String summary;

    @Column(name = "summary_en", columnDefinition = "text")
    private String summaryEn;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
