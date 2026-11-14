package com.portfoliohub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "portfolio_visits")
@Getter
@Setter
public class PortfolioVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Column(name = "visited_at", nullable = false)
    private Instant visitedAt;

    @Column(length = 160)
    private String path;

    @Column(length = 20)
    private String device;

    @Column(length = 255)
    private String referrer;

    @Column(name = "country_code", length = 2)
    private String countryCode;
}
