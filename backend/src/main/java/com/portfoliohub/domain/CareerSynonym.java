package com.portfoliohub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "career_synonyms")
@Getter
@Setter
public class CareerSynonym {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "career_id", nullable = false)
    private UUID careerId;

    @Column(nullable = false, length = 180)
    private String synonym;

    @Column(nullable = false, length = 8)
    private String locale = "es";
}
