package com.portfoliohub.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SpecializationEngine {

    private static final Set<String> SOFTWARE = Set.of(
            "ING-SIS", "ING-INF", "ING-SW", "CIEN-DATOS", "INTEL-ART", "CIBERSEG", "COMP-CIEN", "ING-REDES", "DEV-VIDEO", "DIS-UX"
    );
    private static final Set<String> MEDICINE = Set.of("MED-HUM");
    private static final Set<String> PSYCHOLOGY = Set.of("PSICO", "PSICOPED");
    private static final Set<String> LAW = Set.of("DERECHO");
    private static final Set<String> VISUAL = Set.of("ARQ", "URB", "DIS-INT", "DIS-GRAF", "DIS-IND", "ART-VIS", "FOTO", "CINE");
    private static final Set<String> BUSINESS = Set.of("ADMIN", "MARKET", "NEGOCIOS", "RRHH", "FINAN", "CONT", "ECON", "NEG-INT", "ING-COM");
    private static final Set<String> HEALTH = Set.of(
            "MED-HUM", "ENFER", "ODONT", "OBSTET", "NUTRI", "PSICO", "FARMA", "TEC-MED", "TER-FIS", "FONO", "OPTOM", "SALUD-PUB", "VET-MED"
    );

    public Blueprint resolve(String careerCode, String facultyCode) {
        String career = careerCode == null ? "" : careerCode;
        String faculty = facultyCode == null ? "" : facultyCode;
        List<SectionDef> middle;
        boolean studentSafe = false;
        String disclaimer = null;
        List<String> competencies;
        List<String> examples;

        if (SOFTWARE.contains(career)) {
            middle = software();
            competencies = List.of("Programación", "Modelado de software", "Bases de datos", "Control de versiones", "Trabajo en equipo");
            examples = List.of("Proyecto de curso con repositorio", "Diagrama de arquitectura", "Certificación técnica", "Servicio que puedes ofrecer en formación");
        } else if (MEDICINE.contains(career)) {
            middle = medicine();
            studentSafe = true;
            disclaimer = "Este portafolio presenta formación académica. No acredita colegiatura ni habilitación para el ejercicio de la medicina. No publiques datos de pacientes.";
            competencies = List.of("Lectura científica", "Razonamiento clínico formativo", "Ética", "Comunicación", "Trabajo en equipo");
            examples = List.of("Curso universitario aprobado", "Congreso como asistente", "Investigación formativa", "Voluntariado en salud");
        } else if (PSYCHOLOGY.contains(career)) {
            middle = psychology();
            studentSafe = true;
            disclaimer = "Este portafolio presenta formación académica. No acredita habilitación profesional. No publiques información de personas atendidas ni datos clínicos identificables.";
            competencies = List.of("Investigación", "Lectura crítica", "Ética", "Comunicación", "Trabajo supervisado");
            examples = List.of("Investigación formativa", "Práctica supervisada anonimizada", "Congreso", "Voluntariado");
        } else if (LAW.contains(career) || "LAW".equals(faculty)) {
            middle = law();
            studentSafe = true;
            disclaimer = "Este portafolio presenta formación jurídica. No acredita colegiatura ni habilitación para el ejercicio de la abogacía. Los análisis de casos deben estar anonimizados.";
            competencies = List.of("Argumentación", "Investigación jurídica", "Redacción", "Ética", "Análisis normativo");
            examples = List.of("Artículo académico", "Concurso de debate", "Análisis de caso anonimizado", "Práctica preprofesional");
        } else if (VISUAL.contains(career) || "ARCH".equals(faculty) || "ART".equals(faculty)) {
            middle = visual("ARCH".equals(faculty) || "ARQ".equals(career) || "URB".equals(career));
            competencies = List.of("Composición", "Representación", "Herramientas de diseño", "Proceso de proyecto", "Crítica");
            examples = List.of("Proyecto de taller", "Lámina o render", "Proceso de diseño", "Investigación visual");
        } else if (BUSINESS.contains(career) || "BUS".equals(faculty) || "ECON".equals(faculty)) {
            middle = business();
            competencies = List.of("Análisis", "Planificación", "Comunicación", "Trabajo en equipo", "Lectura de información");
            examples = List.of("Plan de negocio de curso", "Investigación de mercado", "Proyecto de marketing", "Certificación");
        } else if (HEALTH.contains(career) || "SALUD".equals(faculty) || "MED".equals(faculty) || "VET".equals(faculty)) {
            middle = health();
            studentSafe = true;
            disclaimer = "Este portafolio presenta formación académica. No acredita habilitación profesional ni colegiatura. No publiques datos de pacientes o personas atendidas.";
            competencies = List.of("Formación científica", "Ética", "Comunicación", "Trabajo en equipo", "Aprendizaje continuo");
            examples = List.of("Curso aprobado", "Congreso", "Voluntariado", "Investigación formativa");
        } else {
            middle = byFaculty(faculty);
            competencies = defaultCompetencies(faculty);
            examples = List.of("Proyecto de curso", "Curso o certificación", "Investigación o publicación", "Experiencia formativa");
        }
        return new Blueprint(studentSafe, disclaimer, competencies, examples, wrap(middle));
    }

    private List<SectionDef> wrap(List<SectionDef> middle) {
        List<SectionDef> all = new ArrayList<>();
        all.add(section("hero", "Presentación", "hero", "general", "Una frase clara sobre tu formación y lo que quieres demostrar."));
        all.add(section("about", "Sobre mí", "richtext", "general", "Escribe en primera persona. Puedes mostrar proyectos y aprendizajes aunque aún no tengas empleo."));
        all.addAll(middle);
        all.add(section("achievements", "Logros", "achievements", "general", "Premios, concursos, publicaciones o proyectos terminados. No uses porcentajes de dominio."));
        all.add(section("contact", "Contacto", "contact", "general", "Decide qué datos serán visibles solo cuando publiques."));
        return all;
    }

    private List<SectionDef> software() {
        return List.of(
                section("projects", "Proyectos de software", "projects", "software", "Describe el problema, tu rol y el resultado. Enlaza el repositorio si puedes mostrarlo."),
                section("stack", "Tecnologías dominadas", "skills", "tools", "Lenguajes, frameworks y bases de datos que has usado en proyectos reales."),
                section("architecture", "Arquitectura de software", "projects", "architecture", "Explica decisiones de diseño solo en proyectos donde hayas participado."),
                section("repositories", "Repositorios", "projects", "repositories", "Se muestran los proyectos que tengan un enlace de GitHub o GitLab."),
                section("courses", "Cursos completados", "courses", "completed", "Incluye cursos universitarios o externos que puedas describir."),
                section("certifications", "Certificaciones", "certifications", "general", "Revisa cada certificado antes de publicarlo."),
                section("experience", "Experiencia académica", "experiences", "academic", "Cursos, ayudantías, grupos de estudio o prácticas."),
                section("research", "Investigación", "publications", "research", "Trabajos de investigación, tesis o artículos."),
                section("services", "Servicios tecnológicos", "services", "general", "Ofrece solo servicios que realmente puedes prestar. Si aún estudias, preséntalos como servicios en formación.")
        );
    }

    private List<SectionDef> medicine() {
        return List.of(
                section("education", "Formación universitaria", "education", "academic", "Universidad, ciclo y estado académico. El título visible depende de tu estado real."),
                section("courses", "Cursos completados", "courses", "completed", "Asignaturas o cursos que ya concluiste."),
                section("courses_progress", "Cursos en desarrollo", "courses", "progress", "Asignaturas que estás cursando ahora."),
                section("interests", "Áreas de interés médico", "richtext", "general", "Intereses de estudio. No los presentes como especialidad ejercida."),
                section("research", "Participación en investigaciones", "publications", "research", "Rol que cumpliste y el estado del trabajo."),
                section("publications", "Publicaciones científicas", "publications", "citation", "Solo trabajos reales. Incluye autores y medio de publicación."),
                section("conferences", "Congresos y seminarios", "experiences", "conference", "Indica si participaste como asistente, ponente o organizador."),
                section("certifications", "Certificaciones", "certifications", "general", "La confirmación tuya no equivale a una verificación institucional."),
                section("volunteering", "Voluntariados", "experiences", "volunteer", "Describe la actividad sin identificar a pacientes."),
                section("skills", "Habilidades académicas", "skills", "academic", "Habilidades de estudio, investigación y comunicación."),
                section("formative", "Experiencias formativas", "experiences", "formative", "Rotaciones o prácticas formativas, sin datos clínicos identificables.")
        );
    }

    private List<SectionDef> psychology() {
        return List.of(
                section("education", "Perfil académico", "education", "academic", "Tu estado académico se muestra tal como lo registraste."),
                section("interests", "Áreas de interés", "richtext", "general", "Líneas de interés formativo, no ejercicio clínico."),
                section("research", "Investigaciones", "publications", "research", "Trabajos en los que participaste."),
                section("publications", "Publicaciones", "publications", "citation", "Artículos, pósters o tesis."),
                section("courses", "Cursos", "courses", "general", "Cursos concluidos o en desarrollo."),
                section("conferences", "Congresos", "experiences", "conference", "Participación en eventos académicos."),
                section("formative", "Prácticas supervisadas", "experiences", "formative", "Describe la práctica sin identificar a las personas atendidas."),
                section("volunteering", "Voluntariados", "experiences", "volunteer", "Actividades de apoyo que puedas contar con respeto a la confidencialidad."),
                section("skills", "Habilidades de investigación", "skills", "academic", "Métodos y herramientas que has practicado.")
        );
    }

    private List<SectionDef> law() {
        return List.of(
                section("education", "Perfil académico", "education", "academic", "No se te presentará como abogado habilitado por el solo hecho de estudiar la carrera."),
                section("legal_areas", "Áreas de interés jurídico", "areas", "legal", "Áreas de estudio o interés. No implica ejercicio profesional."),
                section("research", "Investigaciones jurídicas", "publications", "research", "Líneas y trabajos de investigación."),
                section("publications", "Artículos académicos", "publications", "citation", "Ensayos, artículos o comentarios publicados o presentados."),
                section("case_studies", "Análisis de casos anonimizados", "publications", "cases", "Omite nombres, números de expediente y cualquier dato que identifique a las personas."),
                section("competitions", "Debates y concursos", "experiences", "competition", "Moot courts, debates o concursos académicos."),
                section("experience", "Experiencia preprofesional", "experiences", "preprofessional", "Prácticas en estudio, institución o clínica jurídica, descritas con reserva."),
                section("certifications", "Certificaciones", "certifications", "general", "Cursos y constancias revisados por ti.")
        );
    }

    private List<SectionDef> visual(boolean architecture) {
        if (architecture) {
            return List.of(
                    section("projects", "Proyectos arquitectónicos", "gallery", "visual", "Muestra el encargo, el concepto y tu participación."),
                    section("plans", "Planos y piezas gráficas", "gallery", "plans", "Sube imágenes que tengas derecho a mostrar."),
                    section("models", "Modelados y maquetas", "gallery", "models", "Renders, fotos de maqueta o modelos tridimensionales."),
                    section("tools", "Programas utilizados", "skills", "tools", "Software que usaste en los proyectos."),
                    section("research", "Investigación", "publications", "research", "Estudios urbanos, memoria descriptiva o investigación de taller."),
                    section("university", "Proyectos universitarios", "gallery", "university", "Trabajos de taller o de curso.")
            );
        }
        return List.of(
                section("gallery", "Galería", "gallery", "visual", "Selecciona piezas que representen tu proceso, no solo el resultado final."),
                section("projects", "Proyectos", "projects", "visual", "Contexto, rol y resultado de cada proyecto."),
                section("tools", "Herramientas", "skills", "tools", "Programas y técnicas que dominas en la práctica."),
                section("process", "Proceso", "richtext", "general", "Cómo investigas, bocetas y decides."),
                section("courses", "Formación", "courses", "general", "Cursos y talleres.")
        );
    }

    private List<SectionDef> business() {
        return List.of(
                section("projects", "Proyectos empresariales", "projects", "business", "Problema, análisis y resultado. Puede ser un trabajo de curso."),
                section("plans", "Planes de negocio", "projects", "plans", "Resume el modelo, el mercado y tu aporte."),
                section("marketing", "Marketing", "projects", "marketing", "Campañas, contenidos o estudios que hayas realizado."),
                section("operations", "Gestión de operaciones", "projects", "operations", "Procesos, indicadores o mejoras que hayas trabajado."),
                section("finance", "Finanzas", "projects", "finance", "Análisis o proyecciones hechas en un contexto académico o real."),
                section("leadership", "Liderazgo", "experiences", "leadership", "Equipos, delegaciones o iniciativas que coordinaste."),
                section("ventures", "Emprendimientos", "projects", "ventures", "Iniciativas propias, aunque estén en etapa temprana."),
                section("research", "Investigación de mercados", "publications", "research", "Estudios y hallazgos."),
                section("certifications", "Certificaciones", "certifications", "general", "Cursos que revisaste antes de incorporar.")
        );
    }

    private List<SectionDef> health() {
        return List.of(
                section("education", "Formación académica", "education", "academic", "Se muestra tu estado real: estudiante, egresado, bachiller, titulado o profesional."),
                section("courses", "Cursos", "courses", "general", "Cursos completados y en desarrollo."),
                section("interests", "Áreas de interés", "richtext", "general", "Intereses de formación, no especialidades ejercidas."),
                section("research", "Investigación", "publications", "research", "Participación en trabajos académicos."),
                section("conferences", "Congresos y seminarios", "experiences", "conference", "Eventos en los que participaste."),
                section("volunteering", "Voluntariados", "experiences", "volunteer", "Sin datos identificables de terceras personas."),
                section("certifications", "Certificaciones", "certifications", "general", "Documentos que tú confirmaste. No son una verificación externa."),
                section("skills", "Habilidades académicas", "skills", "academic", "Competencias de estudio, comunicación y trabajo en equipo.")
        );
    }

    private List<SectionDef> byFaculty(String faculty) {
        return switch (faculty) {
            case "EDU" -> List.of(
                    section("education", "Formación", "education", "academic", "Carrera, ciclo y estado."),
                    section("practice", "Práctica docente", "experiences", "formative", "Aulas o programas en los que practicaste, sin exponer datos de menores."),
                    section("projects", "Proyectos didácticos", "projects", "general", "Secuencias, materiales o propuestas educativas."),
                    section("research", "Investigación", "publications", "research", "Trabajos sobre educación."),
                    section("volunteering", "Voluntariado", "experiences", "volunteer", "Actividades de apoyo educativo."),
                    section("courses", "Cursos", "courses", "general", "Formación complementaria.")
            );
            case "COMM" -> List.of(
                    section("projects", "Piezas y proyectos", "gallery", "visual", "Reportajes, campañas o productos audiovisuales."),
                    section("publications", "Publicaciones", "publications", "citation", "Notas, artículos o guiones difundidos."),
                    section("tools", "Herramientas", "skills", "tools", "Edición, redacción y plataformas."),
                    section("experience", "Experiencia", "experiences", "preprofessional", "Medios, prácticas o proyectos universitarios.")
            );
            case "AGRI", "ENV" -> List.of(
                    section("projects", "Proyectos de campo", "projects", "general", "Estudios, ensayos o intervenciones."),
                    section("research", "Investigación", "publications", "research", "Trabajos académicos o de campo."),
                    section("tools", "Herramientas", "skills", "tools", "Métodos y software utilizados."),
                    section("courses", "Cursos", "courses", "general", "Formación complementaria.")
            );
            case "TOUR" -> List.of(
                    section("projects", "Proyectos", "projects", "business", "Rutas, planes de servicio o estudios de destino."),
                    section("services", "Servicios", "services", "general", "Servicios que puedes describir con honestidad."),
                    section("experience", "Experiencia", "experiences", "preprofessional", "Prácticas en hotelería, turismo o gastronomía."),
                    section("skills", "Idiomas y habilidades", "skills", "general", "Incluye idiomas como habilidades, sin porcentajes.")
            );
            case "SPORT" -> List.of(
                    section("education", "Formación deportiva", "education", "academic", "Formación académica y estado."),
                    section("projects", "Proyectos", "projects", "general", "Programas de entrenamiento, eventos o investigación."),
                    section("experience", "Experiencia", "experiences", "general", "Prácticas, equipos o voluntariado."),
                    section("certifications", "Certificaciones", "certifications", "general", "Cursos y constancias revisados por ti.")
            );
            case "MATH", "EXACT" -> List.of(
                    section("research", "Investigación", "publications", "research", "Problemas, modelos o trabajos teóricos."),
                    section("projects", "Proyectos", "projects", "general", "Aplicaciones, simulaciones o docencia."),
                    section("publications", "Publicaciones", "publications", "citation", "Artículos, pósters o tesis."),
                    section("courses", "Cursos", "courses", "general", "Formación avanzada o complementaria.")
            );
            case "HUM", "SOC", "POL", "IR" -> List.of(
                    section("research", "Investigación", "publications", "research", "Líneas y métodos."),
                    section("publications", "Publicaciones", "publications", "citation", "Ensayos, artículos o tesis."),
                    section("experience", "Trabajo de campo", "experiences", "research", "Prácticas o estudios, con cuidado de los datos personales."),
                    section("courses", "Cursos", "courses", "general", "Seminarios y formación complementaria.")
            );
            default -> List.of(
                    section("education", "Formación", "education", "academic", "Carrera, universidad y estado académico."),
                    section("projects", "Proyectos", "projects", "general", "Trabajos universitarios o personales."),
                    section("skills", "Habilidades", "skills", "general", "Agrupa habilidades por tipo. No indiques porcentajes."),
                    section("courses", "Cursos", "courses", "general", "Cursos y su estado."),
                    section("certifications", "Certificaciones", "certifications", "general", "Documentos revisados por ti."),
                    section("experience", "Experiencia", "experiences", "general", "Prácticas, investigación o voluntariado.")
            );
        };
    }

    private List<String> defaultCompetencies(String faculty) {
        return switch (faculty) {
            case "EDU" -> List.of("Planificación didáctica", "Comunicación", "Evaluación", "Trabajo con grupos");
            case "COMM" -> List.of("Redacción", "Edición", "Investigación", "Narrativa");
            case "MATH", "EXACT" -> List.of("Modelado", "Rigor", "Análisis", "Comunicación técnica");
            case "HUM", "SOC", "POL", "IR" -> List.of("Investigación", "Escritura", "Análisis", "Contexto");
            default -> List.of("Aprendizaje autónomo", "Comunicación", "Organización", "Trabajo en equipo");
        };
    }

    private SectionDef section(String key, String title, String component, String variant, String hint) {
        return new SectionDef(key, title, component, variant, true, hint);
    }
}
