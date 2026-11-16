package com.portfoliohub.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.portfoliohub.common.Texts;
import com.portfoliohub.config.AppProperties;
import com.portfoliohub.domain.Career;
import com.portfoliohub.domain.CareerSynonym;
import com.portfoliohub.domain.CareerTemplateConfig;
import com.portfoliohub.domain.City;
import com.portfoliohub.domain.Country;
import com.portfoliohub.domain.District;
import com.portfoliohub.domain.Faculty;
import com.portfoliohub.domain.PersonalProfile;
import com.portfoliohub.domain.ProfessionalProfile;
import com.portfoliohub.domain.Province;
import com.portfoliohub.domain.Region;
import com.portfoliohub.domain.Role;
import com.portfoliohub.domain.Specialization;
import com.portfoliohub.domain.TemplateDefinition;
import com.portfoliohub.domain.University;
import com.portfoliohub.domain.User;
import com.portfoliohub.repo.CareerRepository;
import com.portfoliohub.repo.CareerSynonymRepository;
import com.portfoliohub.repo.CareerTemplateConfigRepository;
import com.portfoliohub.repo.CityRepository;
import com.portfoliohub.repo.CountryRepository;
import com.portfoliohub.repo.DistrictRepository;
import com.portfoliohub.repo.FacultyRepository;
import com.portfoliohub.repo.PersonalProfileRepository;
import com.portfoliohub.repo.ProfessionalProfileRepository;
import com.portfoliohub.repo.ProvinceRepository;
import com.portfoliohub.repo.RegionRepository;
import com.portfoliohub.repo.RoleRepository;
import com.portfoliohub.repo.SpecializationRepository;
import com.portfoliohub.repo.TemplateDefinitionRepository;
import com.portfoliohub.repo.UniversityRepository;
import com.portfoliohub.repo.UserRepository;
import com.portfoliohub.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(0)
public class CatalogSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogSeeder.class);

    private final RoleRepository roles;
    private final UserRepository users;
    private final PersonalProfileRepository personalProfiles;
    private final ProfessionalProfileRepository professionalProfiles;
    private final CountryRepository countries;
    private final RegionRepository regions;
    private final ProvinceRepository provinces;
    private final CityRepository cities;
    private final DistrictRepository districts;
    private final UniversityRepository universities;
    private final FacultyRepository faculties;
    private final CareerRepository careers;
    private final CareerSynonymRepository synonyms;
    private final SpecializationRepository specializations;
    private final TemplateDefinitionRepository templates;
    private final CareerTemplateConfigRepository configs;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties properties;
    private final ObjectMapper mapper;
    private final SpecializationEngine engine = new SpecializationEngine();

    public CatalogSeeder(
            RoleRepository roles,
            UserRepository users,
            PersonalProfileRepository personalProfiles,
            ProfessionalProfileRepository professionalProfiles,
            CountryRepository countries,
            RegionRepository regions,
            ProvinceRepository provinces,
            CityRepository cities,
            DistrictRepository districts,
            UniversityRepository universities,
            FacultyRepository faculties,
            CareerRepository careers,
            CareerSynonymRepository synonyms,
            SpecializationRepository specializations,
            TemplateDefinitionRepository templates,
            CareerTemplateConfigRepository configs,
            PasswordEncoder passwordEncoder,
            AppProperties properties,
            ObjectMapper mapper
    ) {
        this.roles = roles;
        this.users = users;
        this.personalProfiles = personalProfiles;
        this.professionalProfiles = professionalProfiles;
        this.countries = countries;
        this.regions = regions;
        this.provinces = provinces;
        this.cities = cities;
        this.districts = districts;
        this.universities = universities;
        this.faculties = faculties;
        this.careers = careers;
        this.synonyms = synonyms;
        this.specializations = specializations;
        this.templates = templates;
        this.configs = configs;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        seedRoles();
        if (countries.count() == 0) {
            seedGeography();
        }
        if (universities.count() == 0) {
            seedUniversities();
        }
        if (faculties.count() == 0) {
            seedFaculties();
        }
        if (careers.count() == 0) {
            seedCareers();
        }
        seedTemplates();
        generateMissingConfigs();
        releaseGeneratedPalettes();
        seedAdmin();
        log.info("Catálogo listo: {} carreras, {} plantillas, {} configuraciones.", careers.count(), templates.count(), configs.count());
        if (properties.getMail().isExposeDevLink()) {
            log.warn("El perfil actual muestra enlaces de verificación en la API. No uses esta opción en producción.");
        }
    }

    private void seedRoles() {
        for (String name : List.of("USER", "ADMIN", "MODERATOR")) {
            if (roles.findByName(name).isEmpty()) {
                Role role = new Role();
                role.setName(name);
                roles.save(role);
            }
        }
    }

    private void seedGeography() {
        Map<String, Country> byIso = new HashMap<>();
        for (String line : """
                PE|Perú
                MX|México
                CO|Colombia
                AR|Argentina
                CL|Chile
                EC|Ecuador
                BO|Bolivia
                BR|Brasil
                UY|Uruguay
                PY|Paraguay
                VE|Venezuela
                ES|España
                US|Estados Unidos
                CA|Canadá
                FR|Francia
                DE|Alemania
                IT|Italia
                PT|Portugal
                GB|Reino Unido
                NL|Países Bajos
                BE|Bélgica
                CH|Suiza
                SE|Suecia
                NO|Noruega
                DK|Dinamarca
                FI|Finlandia
                IE|Irlanda
                PL|Polonia
                AT|Austria
                CZ|Chequia
                GR|Grecia
                RO|Rumania
                UA|Ucrania
                TR|Turquía
                RU|Rusia
                CN|China
                JP|Japón
                KR|Corea del Sur
                IN|India
                AU|Australia
                NZ|Nueva Zelanda
                ZA|Sudáfrica
                EG|Egipto
                MA|Marruecos
                NG|Nigeria
                KE|Kenia
                CR|Costa Rica
                PA|Panamá
                GT|Guatemala
                SV|El Salvador
                HN|Honduras
                NI|Nicaragua
                CU|Cuba
                DO|República Dominicana
                PR|Puerto Rico
                """.split("\n")) {
            String[] parts = line.trim().split("\\|");
            if (parts.length != 2) {
                continue;
            }
            Country country = new Country();
            country.setIso2(parts[0]);
            country.setName(parts[1]);
            countries.save(country);
            byIso.put(parts[0], country);
        }
        seedPeru(byIso.get("PE"));
        seedSimple(byIso.get("MX"), "Ciudad de México", "Ciudad de México", "Guadalajara", "Monterrey", "Puebla", "Tijuana");
        seedSimple(byIso.get("CO"), "Bogotá", "Bogotá", "Medellín", "Cali", "Barranquilla", "Cartagena");
        seedSimple(byIso.get("AR"), "Buenos Aires", "Buenos Aires", "Córdoba", "Rosario", "Mendoza");
        seedSimple(byIso.get("CL"), "Santiago", "Santiago", "Valparaíso", "Concepción", "La Serena");
        seedSimple(byIso.get("EC"), "Pichincha", "Quito", "Guayaquil", "Cuenca");
        seedSimple(byIso.get("ES"), "Comunidad de Madrid", "Madrid", "Barcelona", "Valencia", "Sevilla", "Bilbao");
        seedSimple(byIso.get("US"), "California", "Los Ángeles", "San Francisco", "San Diego");
        Region newYork = addRegion(byIso.get("US"), "Nueva York");
        addCity(byIso.get("US"), newYork, null, "Nueva York");
        seedSimple(byIso.get("BR"), "São Paulo", "São Paulo", "Río de Janeiro", "Brasilia", "Belo Horizonte");
    }

    private void seedPeru(Country peru) {
        String[][] data = {
                {"Amazonas", "Chachapoyas"},
                {"Áncash", "Huaraz"},
                {"Apurímac", "Abancay"},
                {"Arequipa", "Arequipa"},
                {"Ayacucho", "Ayacucho"},
                {"Cajamarca", "Cajamarca"},
                {"Callao", "Callao"},
                {"Cusco", "Cusco"},
                {"Huancavelica", "Huancavelica"},
                {"Huánuco", "Huánuco"},
                {"Ica", "Ica"},
                {"Junín", "Huancayo"},
                {"La Libertad", "Trujillo"},
                {"Lambayeque", "Chiclayo"},
                {"Lima", "Lima"},
                {"Loreto", "Iquitos"},
                {"Madre de Dios", "Puerto Maldonado"},
                {"Moquegua", "Moquegua"},
                {"Pasco", "Cerro de Pasco"},
                {"Piura", "Piura"},
                {"Puno", "Puno"},
                {"San Martín", "Moyobamba"},
                {"Tacna", "Tacna"},
                {"Tumbes", "Tumbes"},
                {"Ucayali", "Pucallpa"}
        };
        for (String[] row : data) {
            Region region = addRegion(peru, row[0]);
            Province province = addProvince(region, row[0]);
            City capital = addCity(peru, region, province, row[1]);
            if ("Lima".equals(row[0])) {
                addCity(peru, region, province, "Huacho");
                for (String district : List.of(
                        "Miraflores", "San Isidro", "Santiago de Surco", "La Molina", "Barranco", "San Borja",
                        "Jesús María", "Lince", "Magdalena del Mar", "Pueblo Libre", "San Miguel", "Surquillo",
                        "Los Olivos", "San Martín de Porres", "Independencia", "Comas", "Ate", "Santa Anita",
                        "San Juan de Lurigancho", "El Agustino", "Rímac", "Breña", "Cercado de Lima",
                        "Chorrillos", "Villa El Salvador", "Villa María del Triunfo", "San Juan de Miraflores"
                )) {
                    District item = new District();
                    item.setCityId(capital.getId());
                    item.setName(district);
                    districts.save(item);
                }
            }
        }
    }

    private void seedSimple(Country country, String regionName, String... cityNames) {
        Region region = addRegion(country, regionName);
        for (String cityName : cityNames) {
            addCity(country, region, null, cityName);
        }
    }

    private Region addRegion(Country country, String name) {
        Region region = new Region();
        region.setCountryId(country.getId());
        region.setName(name);
        return regions.save(region);
    }

    private Province addProvince(Region region, String name) {
        Province province = new Province();
        province.setRegionId(region.getId());
        province.setName(name);
        return provinces.save(province);
    }

    private City addCity(Country country, Region region, Province province, String name) {
        City city = new City();
        city.setCountryId(country.getId());
        city.setRegionId(region.getId());
        if (province != null) {
            city.setProvinceId(province.getId());
        }
        city.setName(name);
        return cities.save(city);
    }

    private void seedUniversities() {
        String data = """
                Universidad Nacional Mayor de San Marcos|PE|Lima|https://unmsm.edu.pe
                Universidad Nacional de Ingeniería|PE|Lima|https://www.uni.edu.pe
                Pontificia Universidad Católica del Perú|PE|Lima|https://www.pucp.edu.pe
                Universidad Peruana Cayetano Heredia|PE|Lima|https://cayetano.edu.pe
                Universidad Nacional Agraria La Molina|PE|Lima|https://www.lamolina.edu.pe
                Universidad de Lima|PE|Lima|https://www.ulima.edu.pe
                Universidad Peruana de Ciencias Aplicadas|PE|Lima|https://www.upc.edu.pe
                Universidad del Pacífico|PE|Lima|https://www.up.edu.pe
                Universidad Nacional de San Antonio Abad del Cusco|PE|Cusco|https://www.unsaac.edu.pe
                Universidad Nacional de Trujillo|PE|Trujillo|https://www.unitru.edu.pe
                Universidad Nacional de San Agustín|PE|Arequipa|https://www.unsa.edu.pe
                Universidad de Piura|PE|Piura|https://www.udep.edu.pe
                Universidad Nacional de Colombia|CO|Bogotá|https://unal.edu.co
                Universidad de los Andes|CO|Bogotá|https://uniandes.edu.co
                Universidad Nacional Autónoma de México|MX|Ciudad de México|https://www.unam.mx
                Tecnológico de Monterrey|MX|Monterrey|https://tec.mx
                Universidad de Buenos Aires|AR|Buenos Aires|https://www.uba.ar
                Universidad de Chile|CL|Santiago|https://www.uchile.cl
                Pontificia Universidad Católica de Chile|CL|Santiago|https://www.uc.cl
                Universidad de São Paulo|BR|São Paulo|https://www5.usp.br
                Universidad Complutense de Madrid|ES|Madrid|https://www.ucm.es
                Universitat de Barcelona|ES|Barcelona|https://www.ub.edu
                Massachusetts Institute of Technology|US|Cambridge|https://www.mit.edu
                Stanford University|US|Stanford|https://www.stanford.edu
                """;
        Map<String, Country> byIso = new HashMap<>();
        countries.findAll().forEach(country -> byIso.put(country.getIso2(), country));
        for (String line : data.split("\n")) {
            String[] parts = line.trim().split("\\|");
            if (parts.length != 4) {
                continue;
            }
            University university = new University();
            university.setName(parts[0]);
            Country country = byIso.get(parts[1]);
            if (country != null) {
                university.setCountryId(country.getId());
            }
            university.setCity(parts[2]);
            university.setWebsite(parts[3]);
            universities.save(university);
        }
    }

    private void seedFaculties() {
        String data = """
                ENG|Ingeniería y Tecnología|Engineering and Technology|07
                SALUD|Ciencias de la Salud|Health Sciences|09
                MED|Ciencias Médicas|Medical Sciences|091
                BIO|Ciencias Biológicas|Biological Sciences|05
                NAT|Ciencias Naturales|Natural Sciences|05
                EXACT|Ciencias Exactas|Exact Sciences|05
                SOC|Ciencias Sociales|Social Sciences|03
                LAW|Derecho y Ciencias Jurídicas|Law|042
                ECON|Ciencias Económicas|Economics|031
                BUS|Administración y Negocios|Business and Administration|041
                EDU|Educación|Education|01
                HUM|Humanidades y Letras|Humanities|02
                ART|Artes y Diseño|Arts and Design|021
                ARCH|Arquitectura y Urbanismo|Architecture and Urbanism|073
                COMM|Comunicación y Periodismo|Communication and Journalism|032
                AGRI|Ciencias Agrarias|Agricultural Sciences|08
                VET|Ciencias Veterinarias|Veterinary Sciences|084
                ENV|Ciencias Ambientales|Environmental Sciences|052
                POL|Ciencias Políticas|Political Science|031
                IR|Relaciones Internacionales|International Relations|031
                TOUR|Turismo y Hotelería|Tourism and Hospitality|101
                SPORT|Ciencias del Deporte|Sport Science|101
                MATH|Matemáticas y Estadística|Mathematics and Statistics|054
                """;
        for (String line : data.split("\n")) {
            String[] parts = line.trim().split("\\|");
            Faculty faculty = new Faculty();
            faculty.setCode(parts[0]);
            faculty.setName(parts[1]);
            faculty.setNameEn(parts[2]);
            faculty.setIscedBroad(parts[3]);
            faculty.setDescription("Área académica de referencia. Puede convivir con otras denominaciones institucionales.");
            faculties.save(faculty);
        }
    }

    private void seedCareers() throws Exception {
        Map<String, Faculty> byCode = new HashMap<>();
        faculties.findAll().forEach(faculty -> byCode.put(faculty.getCode(), faculty));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource("catalog/careers.txt").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                Career career = new Career();
                career.setCode(parts[0]);
                career.setName(parts[1]);
                career.setNameEn(parts[2]);
                career.setIscedCode(parts[3]);
                career.setDescription("Carrera del catálogo inicial. Un administrador puede editarla, traducirla o desactivarla.");
                for (String facultyCode : parts[4].split(",")) {
                    Faculty faculty = byCode.get(facultyCode.trim());
                    if (faculty != null) {
                        career.getFaculties().add(faculty);
                    }
                }
                careers.save(career);
                if (!parts[2].isBlank()) {
                    CareerSynonym synonym = new CareerSynonym();
                    synonym.setCareerId(career.getId());
                    synonym.setSynonym(parts[2]);
                    synonym.setLocale("en");
                    synonyms.save(synonym);
                }
                if (parts.length > 5 && !parts[5].isBlank()) {
                    for (String name : parts[5].split(";")) {
                        Specialization specialization = new Specialization();
                        specialization.setCareerId(career.getId());
                        specialization.setName(name.trim());
                        specializations.save(specialization);
                    }
                }
            }
        }
    }

    private void seedTemplates() {
        List<TemplateSeed> seeds = List.of(
                new TemplateSeed("minimal", "Minimalista profesional", "Una columna amplia, tipografía clara y pocos elementos.", "minimal", theme("#1C1915", "#1C1915", "#F7F4EF", "#FFFFFF", "#1C1915", "#5E584E", "Fraunces", "Outfit", "light", "0", "square")),
                new TemplateSeed("corporate", "Corporativo elegante", "Cabecera sobria y lectura ordenada para perfiles institucionales.", "corporate", theme("#1E3A5F", "#C4A574", "#F4F6F8", "#FFFFFF", "#172033", "#5C6B7A", "Libre Baskerville", "Source Sans 3", "light", "8", "solid")),
                new TemplateSeed("tech", "Moderno tecnológico", "Rejilla de proyectos, etiquetas técnicas y acento de código.", "tech", theme("#0F766E", "#99F6E4", "#F3F7F6", "#FFFFFF", "#10211E", "#4B635E", "Space Grotesk", "IBM Plex Sans", "light", "16", "pill")),
                new TemplateSeed("editorial", "Editorial", "Titulares grandes y ritmo de revista.", "editorial", theme("#9A3412", "#1C1915", "#F6F1E8", "#FFFCF7", "#1C1915", "#6B5E51", "Fraunces", "Newsreader", "light", "0", "outline")),
                new TemplateSeed("creative", "Creativo visual", "Bloques de color y galería como eje del portafolio.", "creative", theme("#BE185D", "#FDE68A", "#FFF7FB", "#FFFFFF", "#1F1220", "#6D5364", "Sora", "Manrope", "light", "24", "pill")),
                new TemplateSeed("academic", "Académico", "Composición de papel, citas y trayectoria formativa.", "academic", theme("#1E3A5F", "#7F1D1D", "#F7F3EA", "#FFFcf5", "#1C1915", "#5C564C", "Libre Baskerville", "Source Serif 4", "light", "2", "square")),
                new TemplateSeed("executive", "Ejecutivo", "Banda oscura, datos concretos y lectura rápida.", "executive", theme("#B45309", "#1C1915", "#F6F5F2", "#FFFFFF", "#1C1915", "#5F5A52", "Cormorant Garamond", "Manrope", "light", "8", "solid")),
                new TemplateSeed("experimental", "Experimental", "Numeración visible y composición asimétrica.", "experimental", theme("#4338CA", "#F97316", "#F8F7FF", "#FFFFFF", "#17141F", "#5C5870", "Space Grotesk", "Outfit", "light", "0", "outline")),
                new TemplateSeed("dark", "Elegante oscuro", "Fondo profundo, texto claro y bordes finos.", "dark", theme("#D6B06A", "#F3E7D3", "#121417", "#1C1F24", "#F6F1E8", "#B7B1A6", "Cormorant Garamond", "Outfit", "dark", "12", "outline")),
                new TemplateSeed("premium", "Premium contemporáneo", "Portada amplia y secciones con mucho aire.", "premium", theme("#0F5C45", "#1C1915", "#F3EFE7", "#FFFcf8", "#1C1915", "#5E584E", "Fraunces", "Manrope", "light", "18", "solid")),
                new TemplateSeed("studio", "Estudio de marca", "El nombre ocupa una columna y el relato la otra, como un estudio.", "studio", theme("#111111", "#E11D48", "#F6F4EF", "#FFFFFF", "#161616", "#5C5852", "Fraunces", "Manrope", "light", "0", "square")),
                new TemplateSeed("exhibit", "Sala de exposición", "Marcos amplios para las piezas, al modo de una galería.", "exhibit", theme("#1C1915", "#A16207", "#F3EFE6", "#FFFcf8", "#1C1915", "#5E584E", "Cormorant Garamond", "Outfit", "light", "0", "outline")),
                new TemplateSeed("interior", "Proyecto de interiores", "Portada a sangre y ficha del espacio.", "interior", theme("#3F3A34", "#D6C4A8", "#EFE8DC", "#F7F3EC", "#241F1A", "#6B6258", "Libre Baskerville", "Source Sans 3", "light", "2", "solid")),
                new TemplateSeed("screen", "Retrato cinematográfico", "El nombre se lee sobre una banda oscura.", "screen", theme("#F4E7C5", "#8B1E3F", "#101014", "#17171C", "#F6F1E8", "#C8C0B4", "Cormorant Garamond", "Outfit", "dark", "0", "outline")),
                new TemplateSeed("fashion", "Editorial de moda", "Tipografía alta y las piezas en una franja.", "fashion", theme("#111111", "#F5F5F4", "#F7F6F3", "#FFFFFF", "#111111", "#6B6760", "Newsreader", "Manrope", "light", "0", "square")),
                new TemplateSeed("gazette", "Diario de investigación", "Columnas y filetes, con el ritmo de un periódico.", "gazette", theme("#1A2744", "#9F1239", "#F4F1E8", "#FFFcf6", "#1A1A1A", "#4B5563", "Libre Baskerville", "Source Serif 4", "light", "0", "square"))
        );
        int order = 1;
        for (TemplateSeed seed : seeds) {
            if (templates.findByCode(seed.code()).isPresent()) {
                order++;
                continue;
            }
            TemplateDefinition template = new TemplateDefinition();
            template.setCode(seed.code());
            template.setName(seed.name());
            template.setDescription(seed.description());
            template.setLayout(seed.layout());
            template.setThemeDefaults(seed.theme());
            template.setSortOrder(order++);
            templates.save(template);
        }
    }

    private ObjectNode theme(String accent, String accent2, String background, String surface, String text, String muted, String heading, String body, String mode, String radius, String button) {
        ObjectNode node = mapper.createObjectNode();
        node.put("accent", accent);
        node.put("accent2", accent2);
        node.put("background", background);
        node.put("surface", surface);
        node.put("text", text);
        node.put("muted", muted);
        node.put("fontHeading", heading);
        node.put("fontBody", body);
        node.put("mode", mode);
        node.put("radius", radius);
        node.put("buttonStyle", button);
        node.put("animation", "subtle");
        node.put("density", "comfortable");
        return node;
    }

    private void generateMissingConfigs() {
        List<TemplateDefinition> templateList = templates.findByActiveTrueOrderBySortOrderAsc();
        Blueprint general = engine.resolve(null, null);
        int generalOrder = 1;
        for (TemplateDefinition template : templateList) {
            if (configs.findByCareerIdIsNullAndTemplateId(template.getId()).isEmpty()) {
                saveConfig(null, "Perfil profesional", template, general, generalOrder);
            }
            generalOrder++;
        }
        for (Career career : careers.findAll()) {
            String facultyCode = career.getFaculties().stream().map(Faculty::getCode).findFirst().orElse(null);
            Blueprint blueprint = engine.resolve(career.getCode(), facultyCode);
            int order = 1;
            for (TemplateDefinition template : templateList) {
                if (configs.findByCareerIdAndTemplateId(career.getId(), template.getId()).isEmpty()) {
                    saveConfig(career.getId(), career.getName(), template, blueprint, order);
                }
                order++;
            }
        }
    }

    private void saveConfig(java.util.UUID careerId, String label, TemplateDefinition template, Blueprint blueprint, int order) {
        CareerTemplateConfig config = new CareerTemplateConfig();
        config.setCareerId(careerId);
        config.setTemplateId(template.getId());
        config.setVariantName(label + " · " + template.getName());
        config.setSectionSchema(mapper.valueToTree(blueprint.sections()));
        config.setThemeOverrides(mapper.createObjectNode());
        config.setCompetencies(mapper.valueToTree(blueprint.competencies()));
        config.setContentExamples(mapper.valueToTree(blueprint.examples()));
        config.setDisclaimer(blueprint.disclaimer());
        config.setStudentSafe(blueprint.studentSafe());
        config.setSortOrder(order);
        config.setGenerated(true);
        configs.save(config);
    }

    private void releaseGeneratedPalettes() {
        for (CareerTemplateConfig config : configs.findAll()) {
            if (!config.isGenerated() || config.getThemeOverrides() == null || !config.getThemeOverrides().has("accent")) {
                continue;
            }
            config.setThemeOverrides(mapper.createObjectNode());
            configs.save(config);
        }
    }

    private void seedAdmin() {
        String email = properties.getAdmin().getEmail();
        String password = properties.getAdmin().getPassword();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return;
        }
        if (users.findByEmailIgnoreCase(email.trim()).isPresent()) {
            return;
        }
        Texts.checkPassword(password);
        User admin = new User();
        admin.setEmail(email.trim().toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setFirstName("Administración");
        admin.setLastName("Portfolio Hub");
        admin.setCountryCode("PE");
        admin.setEmailVerified(true);
        admin.setEnabled(true);
        admin.setTermsAcceptedAt(Instant.now());
        admin.setTermsVersion(AuthService.TERMS_VERSION);
        admin.setOnboardingCompleted(true);
        admin.getRoles().add(roles.findByName("ADMIN").orElseThrow());
        admin.getRoles().add(roles.findByName("USER").orElseThrow());
        users.save(admin);
        PersonalProfile personal = new PersonalProfile();
        personal.setUserId(admin.getId());
        personalProfiles.save(personal);
        ProfessionalProfile professional = new ProfessionalProfile();
        professional.setUserId(admin.getId());
        professionalProfiles.save(professional);
        log.warn("Se creó el administrador {}. Cambia la contraseña si este entorno no es local.", admin.getEmail());
    }

    private record TemplateSeed(String code, String name, String description, String layout, ObjectNode theme) {
    }
}
