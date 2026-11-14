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
@Table(name = "universities")
@Getter
@Setter
public class University extends Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 220)
    private String name;

    @Column(name = "country_id")
    private UUID countryId;

    @Column(length = 120)
    private String city;

    @Column(length = 255)
    private String website;

    @Column(nullable = false)
    private boolean active = true;
}
