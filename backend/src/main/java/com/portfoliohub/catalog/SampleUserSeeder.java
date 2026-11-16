package com.portfoliohub.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.portfoliohub.domain.AcademicStatus;
import com.portfoliohub.domain.PortfolioSection;
import com.portfoliohub.domain.CourseStatus;
import com.portfoliohub.domain.ExperienceKind;
import com.portfoliohub.domain.ProjectStatus;
import com.portfoliohub.domain.PublicationKind;
import com.portfoliohub.domain.SkillCategory;
import com.portfoliohub.domain.User;
import com.portfoliohub.repo.CareerRepository;
import com.portfoliohub.repo.CountryRepository;
import com.portfoliohub.repo.FacultyRepository;
import com.portfoliohub.repo.ProjectRepository;
import com.portfoliohub.repo.TemplateDefinitionRepository;
import com.portfoliohub.repo.UniversityRepository;
import com.portfoliohub.repo.UserRepository;
import com.portfoliohub.service.AuthService;
import com.portfoliohub.service.ContentService;
import com.portfoliohub.service.PortfolioService;
import com.portfoliohub.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Order(20)
public class SampleUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleUserSeeder.class);

    private static final String PASSWORD = "Muestra-2026";

    private final AuthService auth;
    private final ProfileService profiles;
    private final ContentService content;
    private final PortfolioService portfolios;
    private final UserRepository users;
    private final FacultyRepository faculties;
    private final CareerRepository careers;
    private final UniversityRepository universities;
    private final CountryRepository countries;
    private final TemplateDefinitionRepository templates;
    private final ProjectRepository projects;
    private final Environment environment;
    private final ObjectMapper mapper;

    public SampleUserSeeder(
            AuthService auth,
            ProfileService profiles,
            ContentService content,
            PortfolioService portfolios,
            UserRepository users,
            FacultyRepository faculties,
            CareerRepository careers,
            UniversityRepository universities,
            CountryRepository countries,
            TemplateDefinitionRepository templates,
            ProjectRepository projects,
            Environment environment,
            ObjectMapper mapper
    ) {
        this.auth = auth;
        this.profiles = profiles;
        this.content = content;
        this.portfolios = portfolios;
        this.users = users;
        this.faculties = faculties;
        this.careers = careers;
        this.universities = universities;
        this.countries = countries;
        this.templates = templates;
        this.projects = projects;
        this.environment = environment;
        this.mapper = mapper;
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        if (environment.matchesProfiles("prod")) {
            return;
        }
        log.info("Creando los diez portafolios de muestra.");
        UUID peru = countries.findByIso2IgnoreCase("PE").orElseThrow().getId();
        for (Person person : people()) {
            User user = users.findByEmailIgnoreCase(person.email).orElseGet(() -> register(person));
            profiles.update(user.getId(), profile(person, peru));
            profiles.updateAcademic(user.getId(), academic(person));
            if (projects.findByUserIdOrderByCreatedAtDesc(user.getId()).isEmpty()) {
                fill(user.getId(), person);
            }
            Map<String, Object> editor = portfolios.applyTemplate(user.getId(), templateId(person.layout));
            @SuppressWarnings("unchecked")
            Map<String, Object> summary = (Map<String, Object>) editor.get("portfolio");
            long version = ((Number) summary.get("version")).longValue();
            portfolios.updateSettings(user.getId(), new PortfolioService.SettingsUpdate(
                    version,
                    "Portafolio de " + person.first + " " + person.last,
                    null,
                    false,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    person.first + " " + person.last,
                    person.bio.substring(0, Math.min(180, person.bio.length()))
            ));
            narrate(user.getId(), person);
        }
    }

    @SuppressWarnings("unchecked")
    private void narrate(UUID userId, Person person) {
        Map<String, Object> editor = portfolios.editor(userId);
        Map<String, Object> summary = (Map<String, Object>) editor.get("portfolio");
        long version = ((Number) summary.get("version")).longValue();
        List<PortfolioSection> current = (List<PortfolioSection>) editor.get("sections");
        List<PortfolioService.SectionUpdate> updates = new ArrayList<>();
        for (PortfolioSection section : current) {
            if (!"richtext".equals(section.getComponent()) && !"areas".equals(section.getComponent())) {
                continue;
            }
            ObjectNode content = mapper.createObjectNode();
            String title = section.getTitle() == null ? "" : section.getTitle().toLowerCase(java.util.Locale.ROOT);
            if ("areas".equals(section.getComponent())) {
                content.putArray("items").addObject().put("title", person.interests).put("description", person.goals);
            } else if (title.contains("sobre") || title.contains("perfil")) {
                content.put("body", person.bio);
            } else {
                content.put("body", person.interests + " " + person.goals);
            }
            updates.add(new PortfolioService.SectionUpdate(
                    section.getSectionKey(), section.getTitle(), section.isVisible(), section.getSortOrder(), content
            ));
        }
        if (!updates.isEmpty()) {
            portfolios.updateSections(userId, version, updates);
        }
    }

    private User register(Person person) {
        auth.register(new AuthService.RegisterCommand(
                person.first, person.last, person.email, PASSWORD, PASSWORD, "PE", true
        ), "127.0.0.1");
        return users.findByEmailIgnoreCase(person.email).orElseThrow();
    }

    private ProfileService.ProfileUpdate profile(Person person, UUID peru) {
        return new ProfileService.ProfileUpdate(
                person.first, person.last, person.bio, null, LocalDate.of(2003, 4, 12), false,
                peru, null, null, null, null,
                place(person.region, person.district), null, place(person.city, person.region, person.district), person.district,
                null, person.phone, person.email,
                person.linkedin, person.github, null, person.behance, null, null, null, person.website,
                List.of()
        );
    }

    private ProfileService.AcademicUpdate academic(Person person) {
        UUID universityId = universities.findByActiveTrueAndNameContainingIgnoreCase(person.university, PageRequest.of(0, 5))
                .stream().filter(item -> item.getName().equals(person.university)).findFirst().orElseThrow().getId();
        return new ProfileService.AcademicUpdate(
                universityId, person.university,
                faculties.findByCode(person.faculty).orElseThrow().getId(),
                careers.findByCode(person.career).orElseThrow().getId(),
                null, person.semester, 2022, LocalDate.of(2027, 12, 15), AcademicStatus.ESTUDIANTE,
                person.courses, person.interests, person.goals, false, null, "UNIVERSITY"
        );
    }

    private String place(String value, String... sameAs) {
        if (value == null) {
            return null;
        }
        for (String other : sameAs) {
            if (other != null && other.equalsIgnoreCase(value)) {
                return null;
            }
        }
        return value;
    }

    private UUID templateId(String code) {
        return templates.findByCode(code).orElseThrow().getId();
    }

    private void fill(UUID userId, Person person) {
        content.saveProject(userId, null, new ContentService.ProjectInput(
                person.project, person.projectText, null, person.category,
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 7, 15),
                person.role, person.results, person.note, Map.of(), List.of(), person.technologies,
                ProjectStatus.PUBLISHED, null
        ));
        content.saveCourse(userId, null, new ContentService.CourseInput(
                person.course, person.coursePlace, LocalDate.of(2025, 4, 1), LocalDate.of(2025, 5, 1),
                person.hours, CourseStatus.COMPLETADO, person.courseText, null, null
        ));
        content.saveCertification(userId, null, new ContentService.CertificationInput(
                person.certificate, person.issuer, LocalDate.of(2025, 6, 1), null, null, person.certificateText, null, null
        ));
        content.saveExperience(userId, null, new ContentService.ExperienceInput(
                person.experience, person.organization, person.kind,
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 11, 1), person.experienceText, null
        ));
        content.savePublication(userId, null, new ContentService.PublicationInput(
                person.paper, person.first + " " + person.last, person.venue, LocalDate.of(2025, 11, 1),
                null, person.paperKind, person.paperText, null
        ));
        content.addSkill(userId, person.skillA, person.skillAKind, "Declarada por el estudiante.");
        content.addSkill(userId, person.skillB, person.skillBKind, "Declarada por el estudiante.");
        content.addSkill(userId, "Trabajo en equipo", SkillCategory.SOFT, "Declarada por el estudiante.");
    }

    private List<Person> people() {
        return List.of(
                new Person("Camila", "Rivas", "camila.rivas.muestra@example.com", "+51 987 100 201", "MED", "MED-HUM",
                        "Universidad Nacional Mayor de San Marcos", "Lima", "Lima", "Miraflores", "academic", "8",
                        "Estudiante de Medicina Humana. Reúno formación, cursos y proyectos formativos sobre salud comunitaria, sin presentar casos de pacientes ni una colegiatura que todavía no tengo.",
                        "Semiología, Salud pública, Bioética", "Salud comunitaria y comunicación en formación.", "Completar el internado y seguir una línea de salud pública.",
                        "Feria de hábitos saludables", "Actividad formativa en el campus sobre alimentación y descanso. No incluye datos de pacientes.", "Salud comunitaria", "Coordinación del equipo", "Guía impresa para estudiantes del ciclo.", null, List.of("Educación para la salud"),
                        "Primeros auxilios básicos", "Cruz Roja Peruana", 20, "Curso corto de soporte inicial, distinto de la práctica clínica.",
                        "Curso de bioética clínica", "Facultad de Medicina", "Certificado de un curso formativo revisado por la estudiante.",
                        "Apoyo en campaña de vacunación informativa", "Centro de salud universitario", ExperienceKind.VOLUNTEER, "Orientación general a estudiantes. Sin registro de historias clínicas.",
                        "Notas de una visita comunitaria", "Aula de salud pública", PublicationKind.OTHER, "Relato formativo anonimizado para el curso de salud pública.",
                        "Entrevista clínica formativa", SkillCategory.ACADEMIC, "Educación para la salud", SkillCategory.PROFESSIONAL,
                        "https://www.linkedin.com/in/camila-rivas-muestra", null, null, null),
                new Person("Mateo", "Quispe", "mateo.quispe.muestra@example.com", "+51 987 100 202", "SALUD", "ENFER",
                        "Universidad Peruana Cayetano Heredia", "Lima", "Lima", "San Borja", "gazette", "6",
                        "Estudiante de Enfermería. Documento cuidados, cursos y prácticas formativas. No atiendo de forma independiente ni publico datos de personas hospitalizadas.",
                        "Fundamentos de enfermería, Salud comunitaria, Farmacología básica", "Enfermería comunitaria y educación al cuidador.", "Concluir las prácticas y preparar la tesis sobre cuidado familiar.",
                        "Guía para cuidadores en casa", "Material de apoyo para familiares, escrito con supervisión docente y sin identificar pacientes.", "Cuidado", "Redacción", "Folleto de ocho páginas para el curso.", null, List.of("Educación al cuidador"),
                        "Signos vitales y registro", "Universidad Peruana Cayetano Heredia", 16, "Taller de registro ordenado de signos vitales en simulación.",
                        "Soporte vital básico", "Comité de simulación", "Certificado de simulación, no de ejercicio profesional.",
                        "Práctica comunitaria supervisada", "Red de salud docente", ExperienceKind.FORMATIVE, "Acompañamiento a visitas ya autorizadas por la universidad.",
                        "Bitácora de simulación clínica", "Curso de fundamentos", PublicationKind.OTHER, "Registro de lo aprendido en laboratorio, sin historias reales.",
                        "Valoración de enfermería", SkillCategory.ACADEMIC, "Registro clínico formativo", SkillCategory.PROFESSIONAL,
                        "https://www.linkedin.com/in/mateo-quispe-muestra", null, null, null),
                new Person("Lucía", "Fernández", "lucia.fernandez.muestra@example.com", "+51 987 100 203", "SALUD", "PSICO",
                        "Pontificia Universidad Católica del Perú", "Lima", "Lima", "Barranco", "editorial", "7",
                        "Estudiante de Psicología. Muestro investigación formativa, cursos y talleres. No ofrezco terapia ni publico casos de consultantes.",
                        "Psicología del aprendizaje, Ética, Métodos de investigación", "Psicología educativa y hábitos de estudio.", "Seguir la línea de orientación educativa después del bachillerato.",
                        "Taller de hábitos de estudio", "Cuatro sesiones con compañeros de facultad sobre organización del tiempo, sin expedientes.", "Educación", "Facilitación", "Guía de planificación semanal.", null, List.of("Dinámicas de grupo"),
                        "Introducción a la psicometría", "Pontificia Universidad Católica del Perú", 32, "Curso de lectura de instrumentos, sin aplicación clínica independiente.",
                        "Ética en investigación con personas", "Comité de facultad", "Curso sobre consentimiento y anonimato.",
                        "Apoyo en orientación vocacional", "Bienestar universitario", ExperienceKind.FORMATIVE, "Charlas informativas supervisadas para escolares visitantes.",
                        "Ensayo sobre hábitos de estudio", "Revista de estudiantes", PublicationKind.ARTICLE, "Texto de curso sobre rutinas de lectura, sin datos personales de participantes.",
                        "Entrevista formativa", SkillCategory.ACADEMIC, "Redacción académica", SkillCategory.PROFESSIONAL,
                        "https://www.linkedin.com/in/lucia-fernandez-muestra", null, null, null),
                new Person("Andrés", "Salazar", "andres.salazar.muestra@example.com", "+51 987 100 204", "LAW", "DERECHO",
                        "Universidad de Lima", "Lima", "Lima", "San Isidro", "executive", "9",
                        "Estudiante de Derecho. Publico comentarios de normas y trabajos de aula. No patrocino casos ni me presento como abogado colegiado.",
                        "Derecho constitucional, Argumentación jurídica, Derechos fundamentales", "Derecho constitucional y comentario normativo.", "Egresar y preparar el trabajo sobre acceso a información pública.",
                        "Comentario a una ley publicada", "Análisis de una norma de acceso a la información, con fuentes abiertas y sin expedientes de clientes.", "Investigación jurídica", "Autor", "Informe de doce páginas para el curso.", null, List.of("Investigación normativa"),
                        "Redacción de escritos", "Universidad de Lima", 24, "Taller de estructura de escritos académicos.",
                        "Curso de derechos fundamentales", "Facultad de Derecho", "Certificado de extensión universitaria.",
                        "Consultorio jurídico estudiantil", "Facultad de Derecho", ExperienceKind.FORMATIVE, "Apoyo supervisado en orientación general, sin firma de patrocinio.",
                        "Nota sobre transparencia administrativa", "Boletín de estudiantes", PublicationKind.CASE_ANALYSIS, "Comentario de una resolución pública, sin datos de partes privadas.",
                        "Investigación normativa", SkillCategory.ACADEMIC, "Argumentación", SkillCategory.PROFESSIONAL,
                        "https://www.linkedin.com/in/andres-salazar-muestra", null, null, null),
                new Person("Valeria", "Choque", "valeria.choque.muestra@example.com", "+51 987 100 205", "ENG", "ING-SIS",
                        "Universidad Nacional de Ingeniería", "Lima", "Lima", "Rímac", "tech", "8",
                        "Estudiante de Ingeniería de Sistemas. Reúno proyectos de software, el stack que uso y cursos de la carrera. El prototipo es trabajo universitario.",
                        "Base de datos, Ingeniería de software, Programación web", "Aplicaciones web y diseño de bases de datos.", "Presentar un proyecto de fin de carrera sobre gestión académica.",
                        "Agenda académica del ciclo", "Aplicación para ordenar cursos, entregas y horarios de un estudiante.", "Software", "Desarrollo", "Prototipo con calendario local.", null, List.of("Java", "PostgreSQL"),
                        "Control de versiones con Git", "Universidad Nacional de Ingeniería", 12, "Taller de ramas, revisiones y trabajo en equipo.",
                        "Curso de SQL", "Laboratorio de bases de datos", "Certificado interno del laboratorio.",
                        "Monitoría de programación", "Escuela de Sistemas", ExperienceKind.ACADEMIC, "Horas de consulta para estudiantes de ciclos iniciales.",
                        "Informe del prototipo de agenda", "Curso de ingeniería de software", PublicationKind.OTHER, "Documento de requisitos y pruebas del prototipo.",
                        "Java", SkillCategory.TECHNICAL, "PostgreSQL", SkillCategory.TOOL,
                        "https://www.linkedin.com/in/valeria-choque-muestra", "https://github.com/valeria-choque-muestra", null, null),
                new Person("Diego", "Paredes", "diego.paredes.muestra@example.com", "+51 984 220 306", "ARCH", "ARQ",
                        "Universidad Nacional de San Antonio Abad del Cusco", "Cusco", "Cusco", "Wanchaq", "exhibit", "7",
                        "Estudiante de Arquitectura en Cusco. Muestro anteproyectos, el proceso de dibujo y las decisiones de emplazamiento. Las imágenes de obra todavía no están cargadas.",
                        "Taller de diseño, Construcción, Urbanismo", "Vivienda en ladera y espacio público de barrio.", "Desarrollar el taller de fin de carrera con un lote autorizado por el curso.",
                        "Anteproyecto de vivienda en ladera", "Propuesta académica de una vivienda de dos niveles, con corte, planta y una nota de asoleamiento.", "Taller", "Autor del anteproyecto", "Láminas de planta, corte y memoria breve.", "Planta libre en el primer nivel y muro de contención hacia la ladera.", List.of("Dibujo arquitectónico"),
                        "Representación arquitectónica", "Universidad Nacional de San Antonio Abad del Cusco", 40, "Curso de planta, corte y axonometría.",
                        "Taller de lectura de terreno", "Escuela de Arquitectura", "Salida de campo del curso de urbanismo.",
                        "Apoyo en maquetas del taller", "Taller vertical", ExperienceKind.ACADEMIC, "Armado de maquetas de estudio para la revisión de medio ciclo.",
                        "Memoria del anteproyecto", "Taller de diseño", PublicationKind.OTHER, "Texto que explica el emplazamiento y el programa de la vivienda.",
                        "Dibujo de planos", SkillCategory.TECHNICAL, "Maquetas", SkillCategory.TOOL,
                        "https://www.linkedin.com/in/diego-paredes-muestra", null, "https://www.behance.net/diego-paredes-muestra", null),
                new Person("Renata", "Vidal", "renata.vidal.muestra@example.com", "+51 987 100 207", "ART", "DIS-GRAF",
                        "Pontificia Universidad Católica del Perú", "Lima", "Lima", "Surco", "creative", "6",
                        "Estudiante de Diseño Gráfico. Reúno piezas de identidad, afiches de curso y el proceso de cada encargo universitario. No presento marcas comerciales ajenas como propias.",
                        "Tipografía, Identidad visual, Editorial", "Identidad visual y diseño editorial de corto formato.", "Armar un cuerpo de trabajo para la práctica preprofesional.",
                        "Identidad de una feria de curso", "Sistema gráfico para la feria de proyectos de la facultad: marca, paleta y afiche.", "Identidad", "Diseño", "Manual breve de uso de la marca del evento.", null, List.of("Tipografía", "Composición"),
                        "Tipografía aplicada", "Pontificia Universidad Católica del Perú", 28, "Ejercicios de jerarquía y retícula.",
                        "Taller de afiche", "Escuela de diseño", "Certificado del taller intensivo de una semana.",
                        "Apoyo gráfico del congreso de estudiantes", "Centro de estudiantes", ExperienceKind.FORMATIVE, "Piezas para redes y señalética del evento interno.",
                        "Memoria de la identidad de la feria", "Curso de identidad visual", PublicationKind.OTHER, "Decisiones de color, tipo y aplicaciones.",
                        "Composición", SkillCategory.TECHNICAL, "Tipografía", SkillCategory.ACADEMIC,
                        "https://www.linkedin.com/in/renata-vidal-muestra", null, "https://www.behance.net/renata-vidal-muestra", null),
                new Person("Sofía", "Mendoza", "sofia.mendoza.muestra@example.com", "+51 987 100 208", "BUS", "ADMIN",
                        "Universidad del Pacífico", "Lima", "Lima", "Jesús María", "corporate", "7",
                        "Estudiante de Administración. Organizo trabajos de operaciones, cursos y una experiencia de apoyo en una feria universitaria. No publico cifras de empresas reales.",
                        "Operaciones, Costos, Comportamiento organizacional", "Operaciones y organización de equipos pequeños.", "Llegar a la práctica con un portafolio de casos de curso.",
                        "Plan de la feria de emprendimientos", "Organización de turnos, proveedores del campus y el recorrido de visitantes para una feria de la facultad.", "Operaciones", "Coordinación", "Cronograma y mapa de puestos.", null, List.of("Planificación"),
                        "Hojas de cálculo para gestión", "Universidad del Pacífico", 18, "Curso de tablas y gráficos para informes de curso.",
                        "Taller de trabajo en equipo", "Bienestar universitario", "Certificado de un taller de coordinación.",
                        "Apoyo en admisiones de un evento", "Facultad de Administración", ExperienceKind.FORMATIVE, "Registro de asistentes y entrega de materiales del congreso interno.",
                        "Informe de la feria", "Curso de operaciones", PublicationKind.OTHER, "Qué se planificó, qué ocurrió y qué se ajustaría.",
                        "Planificación", SkillCategory.PROFESSIONAL, "Hojas de cálculo", SkillCategory.TOOL,
                        "https://www.linkedin.com/in/sofia-mendoza-muestra", null, null, null),
                new Person("Hugo", "Castillo", "hugo.castillo.muestra@example.com", "+51 987 100 209", "ART", "DIS-INT",
                        "Universidad Peruana de Ciencias Aplicadas", "Lima", "Lima", "Miraflores", "interior", "5",
                        "Estudiante de Diseño de Interiores. Muestro una propuesta de aula, el criterio de luz y la distribución del mobiliario. Es un encargo de taller, no una obra construida.",
                        "Taller de interiores, Color, Materiales", "Espacios de estudio y luz natural.", "Completar el taller con un segundo espacio, también académico.",
                        "Aula de lectura para cuarenta personas", "Redistribución de un aula del campus: mesas, estantería y luz de tarde.", "Interiores", "Propuesta", "Planta de mobiliario y una memoria de materiales.", "Las mesas largas quedan perpendiculares a la ventana para reducir el reflejo.", List.of("Distribución", "Iluminación"),
                        "Materiales de interior", "Universidad Peruana de Ciencias Aplicadas", 24, "Reconocimiento de maderas, textiles y pinturas de uso interior.",
                        "Taller de iluminación", "Escuela de diseño", "Ejercicio de temperatura de color en un aula.",
                        "Apoyo en el montaje de la exposición de taller", "Facultad de Diseño", ExperienceKind.FORMATIVE, "Colocación de láminas y maquetas de fin de ciclo.",
                        "Memoria del aula de lectura", "Taller de interiores", PublicationKind.OTHER, "Criterios de circulación, luz y mobiliario.",
                        "Distribución de mobiliario", SkillCategory.TECHNICAL, "Dibujo de planta", SkillCategory.TOOL,
                        "https://www.linkedin.com/in/hugo-castillo-muestra", null, "https://www.behance.net/hugo-castillo-muestra", null),
                new Person("Elena", "Navarro", "elena.navarro.muestra@example.com", "+51 944 330 410", "COMM", "PERIOD",
                        "Universidad Nacional de Trujillo", "La Libertad", "Trujillo", "Trujillo", "screen", "6",
                        "Estudiante de Periodismo en Trujillo. Publico reportajes de curso, la edición y las fuentes abiertas que usé. No reproduzco entrevistas sin el acuerdo del taller.",
                        "Reportería, Ética periodística, Edición", "Periodismo local y edición de piezas breves.", "Armar una carpeta de tres reportajes antes de la práctica.",
                        "Reportaje sobre el transporte al campus", "Pieza sobre los tiempos de viaje de estudiantes, con entrevistas acordadas y sin publicar teléfonos privados.", "Reportaje", "Reportería y edición", "Texto de 900 palabras y un esquema de fuentes.", null, List.of("Entrevista", "Edición"),
                        "Ética y fuentes", "Universidad Nacional de Trujillo", 20, "Curso sobre cita de fuentes y protección de personas entrevistadas.",
                        "Taller de crónica breve", "Escuela de periodismo", "Certificado del taller de escritura de mitad de ciclo.",
                        "Pasantía en el medio de la universidad", "Medio universitario", ExperienceKind.PREPROFESSIONAL, "Edición de notas cortas del calendario académico.",
                        "El camino al campus", "Medio universitario", PublicationKind.ARTICLE, "Reportaje de curso sobre tiempos de transporte.",
                        "Entrevista", SkillCategory.PROFESSIONAL, "Edición", SkillCategory.TECHNICAL,
                        "https://www.linkedin.com/in/elena-navarro-muestra", null, null, "https://example.com/elena-navarro-muestra")
        );
    }

    private record Person(
            String first, String last, String email, String phone, String faculty, String career, String university,
            String region, String city, String district, String layout, String semester, String bio, String courses,
            String interests, String goals, String project, String projectText, String category, String role, String results,
            String note, List<String> technologies, String course, String coursePlace, int hours, String courseText,
            String certificate, String issuer, String certificateText, String experience, String organization, ExperienceKind kind,
            String experienceText, String paper, String venue, PublicationKind paperKind, String paperText,
            String skillA, SkillCategory skillAKind, String skillB, SkillCategory skillBKind,
            String linkedin, String github, String behance, String website
    ) {
    }
}
