package com.portfoliohub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "personal_profiles")
@Getter
@Setter
public class PersonalProfile extends Timestamps {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "photo_document_id")
    private UUID photoDocumentId;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "show_birth_date", nullable = false)
    private boolean showBirthDate;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(name = "bio_en", columnDefinition = "text")
    private String bioEn;

    @Column(name = "country_id")
    private UUID countryId;

    @Column(name = "region_id")
    private UUID regionId;

    @Column(name = "province_id")
    private UUID provinceId;

    @Column(name = "city_id")
    private UUID cityId;

    @Column(name = "district_id")
    private UUID districtId;

    @Column(name = "custom_region", length = 120)
    private String customRegion;

    @Column(name = "custom_province", length = 120)
    private String customProvince;

    @Column(name = "custom_city", length = 120)
    private String customCity;

    @Column(name = "custom_district", length = 120)
    private String customDistrict;

    @Column(length = 255)
    private String address;

    @Column(length = 40)
    private String phone;

    @Column(name = "professional_email", length = 255)
    private String professionalEmail;
}
