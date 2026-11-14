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
@Table(name = "faculties")
@Getter
@Setter
public class Faculty extends Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(name = "name_en", length = 180)
    private String nameEn;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "isced_broad", length = 10)
    private String iscedBroad;

    @Column(nullable = false)
    private boolean active = true;
}
