CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(40) NOT NULL UNIQUE
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    country_code VARCHAR(2),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMPTZ,
    terms_accepted_at TIMESTAMPTZ NOT NULL,
    terms_version VARCHAR(20) NOT NULL,
    onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX ux_users_email_active ON users (lower(email)) WHERE deleted_at IS NULL;

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users (id),
    role_id UUID NOT NULL REFERENCES roles (id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE verification_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    type VARCHAR(30) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE outbound_messages (
    id UUID PRIMARY KEY,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE countries (
    id UUID PRIMARY KEY,
    iso2 VARCHAR(2) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE regions (
    id UUID PRIMARY KEY,
    country_id UUID NOT NULL REFERENCES countries (id),
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (country_id, name)
);

CREATE TABLE provinces (
    id UUID PRIMARY KEY,
    region_id UUID NOT NULL REFERENCES regions (id),
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (region_id, name)
);

CREATE TABLE cities (
    id UUID PRIMARY KEY,
    country_id UUID NOT NULL REFERENCES countries (id),
    region_id UUID REFERENCES regions (id),
    province_id UUID REFERENCES provinces (id),
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX ix_cities_country ON cities (country_id);
CREATE INDEX ix_cities_region ON cities (region_id);
CREATE INDEX ix_cities_name ON cities (lower(name));

CREATE TABLE districts (
    id UUID PRIMARY KEY,
    city_id UUID NOT NULL REFERENCES cities (id),
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (city_id, name)
);

CREATE TABLE geo_cache (
    id UUID PRIMARY KEY,
    provider VARCHAR(80) NOT NULL,
    query_key VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    fetched_at TIMESTAMPTZ NOT NULL,
    UNIQUE (provider, query_key)
);

CREATE TABLE universities (
    id UUID PRIMARY KEY,
    name VARCHAR(220) NOT NULL,
    country_id UUID REFERENCES countries (id),
    city VARCHAR(120),
    website VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_universities_name ON universities (lower(name));

CREATE TABLE faculties (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(180) NOT NULL,
    name_en VARCHAR(180),
    description TEXT,
    isced_broad VARCHAR(10),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE careers (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(180) NOT NULL,
    name_en VARCHAR(180),
    description TEXT,
    isced_code VARCHAR(10),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE career_faculties (
    career_id UUID NOT NULL REFERENCES careers (id),
    faculty_id UUID NOT NULL REFERENCES faculties (id),
    PRIMARY KEY (career_id, faculty_id)
);

CREATE TABLE career_synonyms (
    id UUID PRIMARY KEY,
    career_id UUID NOT NULL REFERENCES careers (id),
    synonym VARCHAR(180) NOT NULL,
    locale VARCHAR(8) NOT NULL DEFAULT 'es'
);

CREATE UNIQUE INDEX ux_career_synonym ON career_synonyms (career_id, lower(synonym));

CREATE TABLE specializations (
    id UUID PRIMARY KEY,
    career_id UUID NOT NULL REFERENCES careers (id),
    name VARCHAR(180) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX ux_specialization_name ON specializations (career_id, lower(name));

CREATE TABLE academic_records (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    university_id UUID REFERENCES universities (id),
    university_name VARCHAR(220),
    faculty_id UUID REFERENCES faculties (id),
    career_id UUID REFERENCES careers (id),
    specialization_id UUID REFERENCES specializations (id),
    semester VARCHAR(40),
    admission_year INTEGER,
    expected_graduation DATE,
    status VARCHAR(30) NOT NULL,
    relevant_courses TEXT,
    interests TEXT,
    professional_goals TEXT,
    license_declared BOOLEAN NOT NULL DEFAULT FALSE,
    primary_record BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_academic_status CHECK (status IN ('ESTUDIANTE', 'EGRESADO', 'BACHILLER', 'TITULADO', 'PROFESIONAL', 'POSGRADO')),
    CONSTRAINT ck_admission_year CHECK (admission_year IS NULL OR (admission_year BETWEEN 1950 AND 2100))
);

CREATE INDEX ix_academic_user ON academic_records (user_id);

CREATE TABLE documents (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    original_name VARCHAR(255) NOT NULL,
    storage_key VARCHAR(255) NOT NULL UNIQUE,
    mime_type VARCHAR(120) NOT NULL,
    size_bytes BIGINT NOT NULL,
    category VARCHAR(40) NOT NULL,
    visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    checksum VARCHAR(64),
    scan_status VARCHAR(20) NOT NULL,
    scan_detail VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT ck_doc_visibility CHECK (visibility IN ('PRIVATE', 'PUBLIC')),
    CONSTRAINT ck_doc_size CHECK (size_bytes > 0 AND size_bytes <= 15728640)
);

CREATE INDEX ix_documents_user ON documents (user_id);

CREATE TABLE personal_profiles (
    user_id UUID PRIMARY KEY REFERENCES users (id),
    photo_document_id UUID REFERENCES documents (id),
    birth_date DATE,
    show_birth_date BOOLEAN NOT NULL DEFAULT FALSE,
    bio TEXT,
    bio_en TEXT,
    country_id UUID REFERENCES countries (id),
    region_id UUID REFERENCES regions (id),
    province_id UUID REFERENCES provinces (id),
    city_id UUID REFERENCES cities (id),
    district_id UUID REFERENCES districts (id),
    custom_region VARCHAR(120),
    custom_province VARCHAR(120),
    custom_city VARCHAR(120),
    custom_district VARCHAR(120),
    address VARCHAR(255),
    phone VARCHAR(40),
    professional_email VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE professional_profiles (
    user_id UUID PRIMARY KEY REFERENCES users (id),
    linkedin VARCHAR(255),
    github VARCHAR(255),
    gitlab VARCHAR(255),
    behance VARCHAR(255),
    dribbble VARCHAR(255),
    orcid VARCHAR(40),
    google_scholar VARCHAR(255),
    website VARCHAR(255),
    other_links JSONB,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE document_extractions (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES documents (id),
    extracted_json JSONB NOT NULL,
    status VARCHAR(30) NOT NULL,
    confirmed_json JSONB,
    confirmed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_extraction_status CHECK (status IN ('PENDING_REVIEW', 'CONFIRMED', 'REJECTED'))
);

CREATE TABLE skills (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(30) NOT NULL,
    created_by UUID REFERENCES users (id),
    CONSTRAINT ck_skill_category CHECK (category IN ('TECHNICAL', 'PROFESSIONAL', 'SOFT', 'TOOL', 'ACADEMIC'))
);

CREATE UNIQUE INDEX ux_skills_name_cat ON skills (lower(name), category);

CREATE TABLE user_skills (
    user_id UUID NOT NULL REFERENCES users (id),
    skill_id UUID NOT NULL REFERENCES skills (id),
    note VARCHAR(255),
    PRIMARY KEY (user_id, skill_id)
);

CREATE TABLE courses (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    name VARCHAR(200) NOT NULL,
    institution VARCHAR(200),
    start_date DATE,
    end_date DATE,
    hours INTEGER,
    status VARCHAR(30) NOT NULL,
    description TEXT,
    description_en TEXT,
    certificate_document_id UUID REFERENCES documents (id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT ck_course_status CHECK (status IN ('COMPLETADO', 'EN_CURSO', 'PLANIFICADO')),
    CONSTRAINT ck_course_hours CHECK (hours IS NULL OR hours >= 0)
);

CREATE INDEX ix_courses_user ON courses (user_id);

CREATE TABLE certifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    name VARCHAR(200) NOT NULL,
    issuer VARCHAR(200),
    issued_on DATE,
    expires_on DATE,
    credential_id VARCHAR(120),
    description TEXT,
    description_en TEXT,
    document_id UUID REFERENCES documents (id),
    verification_status VARCHAR(30) NOT NULL DEFAULT 'UNVERIFIED',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT ck_cert_verification CHECK (verification_status IN ('UNVERIFIED', 'USER_CONFIRMED'))
);

CREATE INDEX ix_certifications_user ON certifications (user_id);

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    description_en TEXT,
    category VARCHAR(80),
    start_date DATE,
    end_date DATE,
    role_name VARCHAR(120),
    results TEXT,
    architecture_note TEXT,
    links JSONB,
    participants JSONB,
    publication_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    cover_document_id UUID REFERENCES documents (id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT ck_project_pub CHECK (publication_status IN ('DRAFT', 'PUBLISHED'))
);

CREATE INDEX ix_projects_user ON projects (user_id);

CREATE TABLE project_images (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id),
    document_id UUID NOT NULL REFERENCES documents (id),
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE project_technologies (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id),
    name VARCHAR(80) NOT NULL
);

CREATE TABLE experiences (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    title VARCHAR(200) NOT NULL,
    organization VARCHAR(200),
    kind VARCHAR(40) NOT NULL,
    start_date DATE,
    end_date DATE,
    description TEXT,
    description_en TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT ck_experience_kind CHECK (kind IN ('ACADEMIC', 'PREPROFESSIONAL', 'VOLUNTEER', 'RESEARCH', 'WORK', 'FORMATIVE'))
);

CREATE INDEX ix_experiences_user ON experiences (user_id);

CREATE TABLE publications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    title VARCHAR(255) NOT NULL,
    authors VARCHAR(255),
    venue VARCHAR(255),
    published_on DATE,
    url VARCHAR(255),
    kind VARCHAR(40) NOT NULL,
    summary TEXT,
    summary_en TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT ck_publication_kind CHECK (kind IN ('ARTICLE', 'CONFERENCE', 'THESIS', 'CASE_ANALYSIS', 'OTHER'))
);

CREATE INDEX ix_publications_user ON publications (user_id);

CREATE TABLE templates (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    layout VARCHAR(40) NOT NULL,
    theme_defaults JSONB NOT NULL,
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE career_template_configs (
    id UUID PRIMARY KEY,
    career_id UUID REFERENCES careers (id),
    template_id UUID NOT NULL REFERENCES templates (id),
    variant_name VARCHAR(160) NOT NULL,
    section_schema JSONB NOT NULL,
    theme_overrides JSONB NOT NULL,
    competencies JSONB NOT NULL,
    content_examples JSONB NOT NULL,
    disclaimer TEXT,
    student_safe BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER NOT NULL,
    generated BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX ux_career_template ON career_template_configs (career_id, template_id) WHERE career_id IS NOT NULL;
CREATE UNIQUE INDEX ux_default_template_config ON career_template_configs (template_id) WHERE career_id IS NULL;

CREATE TABLE portfolios (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    title VARCHAR(160) NOT NULL,
    slug VARCHAR(80),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    template_id UUID REFERENCES templates (id),
    career_id UUID REFERENCES careers (id),
    theme JSONB NOT NULL,
    theme_touched BOOLEAN NOT NULL DEFAULT FALSE,
    seo_title VARCHAR(160),
    seo_description VARCHAR(300),
    whatsapp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    show_email BOOLEAN NOT NULL DEFAULT FALSE,
    show_phone BOOLEAN NOT NULL DEFAULT FALSE,
    bilingual BOOLEAN NOT NULL DEFAULT FALSE,
    analytics_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    recruiter_view_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    showcase_opt_in BOOLEAN NOT NULL DEFAULT FALSE,
    consent_to_publish BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT ck_portfolio_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'UNPUBLISHED'))
);

CREATE UNIQUE INDEX ux_portfolio_slug ON portfolios (lower(slug)) WHERE slug IS NOT NULL AND deleted_at IS NULL;
CREATE UNIQUE INDEX ux_one_portfolio_per_user ON portfolios (user_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_portfolio_user ON portfolios (user_id);

CREATE TABLE portfolio_sections (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios (id),
    section_key VARCHAR(60) NOT NULL,
    title VARCHAR(160) NOT NULL,
    component VARCHAR(40) NOT NULL,
    variant VARCHAR(40),
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL,
    content JSONB NOT NULL,
    UNIQUE (portfolio_id, section_key)
);

CREATE TABLE portfolio_versions (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios (id),
    snapshot JSONB NOT NULL,
    label VARCHAR(160),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE portfolio_publications (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios (id),
    slug VARCHAR(80) NOT NULL,
    published_at TIMESTAMPTZ NOT NULL,
    unpublished_at TIMESTAMPTZ,
    unpublished_reason VARCHAR(255)
);

CREATE TABLE portfolio_domains (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios (id),
    domain VARCHAR(255) NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_domain_status CHECK (status IN ('REGISTERED_INTEREST'))
);

CREATE TABLE portfolio_visits (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios (id),
    visited_at TIMESTAMPTZ NOT NULL,
    path VARCHAR(160),
    device VARCHAR(20),
    referrer VARCHAR(255),
    country_code VARCHAR(2)
);

CREATE INDEX ix_visits_portfolio_time ON portfolio_visits (portfolio_id, visited_at);

CREATE TABLE contact_messages (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios (id),
    name VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_contact_portfolio ON contact_messages (portfolio_id, created_at DESC);

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    title VARCHAR(160) NOT NULL,
    body TEXT NOT NULL,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_notifications_user ON notifications (user_id, created_at DESC);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_id UUID,
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id VARCHAR(80),
    details TEXT,
    ip VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_audit_created ON audit_logs (created_at DESC);
