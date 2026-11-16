$ErrorActionPreference = 'Stop'
$api = 'http://127.0.0.1:8080/api/v1'
$password = 'Muestra-2026'

function Send-Api {
    param([string]$Method, [string]$Url, $Body, [string]$Token)
    $headers = @{ Accept = 'application/json' }
    if ($Token) { $headers.Authorization = "Bearer $Token" }
    $params = @{ Method = $Method; Uri = $Url; Headers = $headers }
    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Depth 8 -Compress
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes($json)
        $params.ContentType = 'application/json; charset=utf-8'
    }
    try {
        return Invoke-RestMethod @params
    } catch {
        $detail = $_.ErrorDetails.Message
        if (-not $detail -and $_.Exception.Response) {
            $stream = $_.Exception.Response.GetResponseStream()
            if ($stream) {
                $reader = New-Object System.IO.StreamReader($stream)
                $detail = $reader.ReadToEnd()
            }
        }
        throw "$Method $Url -> $detail"
    }
}

$faculties = @{}
foreach ($item in (Invoke-RestMethod "$api/catalog/faculties")) { $faculties[$item.code] = $item.id }
$careers = @{}
foreach ($code in @('MED-HUM','ENFER','PSICO','DERECHO','ING-SIS','ARQ','DIS-GRAF','ADMIN','DIS-INT','PERIOD')) {
    $page = Invoke-RestMethod "$api/catalog/careers?q=$code&size=20"
    $match = $page.content | Where-Object { $_.code -eq $code } | Select-Object -First 1
    $careers[$code] = $match.id
}
$universities = @{}
foreach ($name in @(
    'Universidad Nacional Mayor de San Marcos',
    'Universidad Peruana Cayetano Heredia',
    'Pontificia Universidad Católica del Perú',
    'Universidad de Lima',
    'Universidad Nacional de Ingeniería',
    'Universidad Nacional de San Antonio Abad del Cusco',
    'Universidad del Pacífico',
    'Universidad Peruana de Ciencias Aplicadas',
    'Universidad Nacional de Trujillo'
)) {
    $encoded = [uri]::EscapeDataString($name)
    $page = Invoke-RestMethod "$api/catalog/universities?q=$encoded&size=10"
    $match = $page.content | Where-Object { $_.name -eq $name } | Select-Object -First 1
    $universities[$name] = $match.id
}
$templates = @{}
foreach ($item in (Invoke-RestMethod "$api/catalog/templates")) { $templates[$item.code] = $item.id }
$peru = (Invoke-RestMethod "$api/catalog/geography/countries") | Where-Object { $_.iso2 -eq 'PE' } | Select-Object -First 1

$people = @(
    @{
        first = 'Camila'; last = 'Rivas'; email = 'camila.rivas.muestra@example.com'; phone = '+51 987 100 201'
        faculty = 'MED'; career = 'MED-HUM'; university = 'Universidad Nacional Mayor de San Marcos'
        region = 'Lima'; city = 'Lima'; district = 'Miraflores'; layout = 'academic'; status = 'ESTUDIANTE'; semester = '8'
        bio = 'Estudiante de Medicina Humana. Reúno formación, cursos y proyectos formativos sobre salud comunitaria, sin presentar casos de pacientes ni una colegiatura que todavía no tengo.'
        interests = 'Salud comunitaria y comunicación con pacientes en formación.'
        goals = 'Completar el internado y seguir una línea de salud pública.'
        courses = 'Semiología, Salud pública, Bioética'
        project = @{ name = 'Feria de hábitos saludables'; description = 'Actividad formativa en el campus sobre alimentación y descanso. No incluye datos de pacientes.'; category = 'Salud comunitaria'; role = 'Coordinación del equipo'; results = 'Guía impresa para estudiantes del ciclo.'; tech = @('Educación para la salud') }
        course = @{ name = 'Primeros auxilios básicos'; institution = 'Cruz Roja Peruana'; hours = 20; status = 'COMPLETADO'; description = 'Curso corto de soporte inicial, distinto de la práctica clínica.' }
        cert = @{ name = 'Curso de bioética clínica'; issuer = 'Facultad de Medicina'; description = 'Certificado de un curso formativo revisado por la estudiante.' }
        experience = @{ title = 'Apoyo en campaña de vacunación informativa'; organization = 'Centro de salud universitario'; kind = 'VOLUNTEER'; description = 'Orientación general a estudiantes. Sin registro de historias clínicas.' }
        publication = @{ title = 'Notas de una visita comunitaria'; venue = 'Aula de salud pública'; kind = 'OTHER'; summary = 'Relato formativo anonimizado para el curso de salud pública.' }
        skills = @(@{ name = 'Entrevista clínica formativa'; category = 'ACADEMIC' }, @{ name = 'Trabajo en equipo'; category = 'SOFT' }, @{ name = 'Educación para la salud'; category = 'PROFESSIONAL' })
        links = @{ linkedin = 'https://www.linkedin.com/in/camila-rivas-muestra' }
    },
    @{
        first = 'Mateo'; last = 'Quispe'; email = 'mateo.quispe.muestra@example.com'; phone = '+51 987 100 202'
        faculty = 'SALUD'; career = 'ENFER'; university = 'Universidad Peruana Cayetano Heredia'
        region = 'Lima'; city = 'Lima'; district = 'San Borja'; layout = 'gazette'; status = 'ESTUDIANTE'; semester = '6'
        bio = 'Estudiante de Enfermería. Documento cuidados, cursos y prácticas formativas con un límite claro: no atiendo de forma independiente ni publico datos de personas hospitalizadas.'
        interests = 'Enfermería comunitaria y educación al cuidador.'
        goals = 'Concluir las prácticas y preparar la tesis sobre cuidado familiar.'
        courses = 'Fundamentos de enfermería, Salud comunitaria, Farmacología básica'
        project = @{ name = 'Guía para cuidadores en casa'; description = 'Material de apoyo para familiares, escrito con supervisión docente y sin identificar pacientes.'; category = 'Cuidado'; role = 'Redacción'; results = 'Folleto de ocho páginas para el curso.'; tech = @('Educación al cuidador') }
        course = @{ name = 'Signos vitales y registro'; institution = 'Universidad Peruana Cayetano Heredia'; hours = 16; status = 'COMPLETADO'; description = 'Taller de registro ordenado de signos vitales en simulación.' }
        cert = @{ name = 'Soporte vital básico'; issuer = 'Comité de simulación'; description = 'Certificado de simulación, no de ejercicio profesional.' }
        experience = @{ title = 'Práctica comunitaria supervisada'; organization = 'Red de salud docente'; kind = 'FORMATIVE'; description = 'Acompañamiento a visitas ya autorizadas por la universidad.' }
        publication = @{ title = 'Bitácora de simulación clínica'; venue = 'Curso de fundamentos'; kind = 'OTHER'; summary = 'Registro de lo aprendido en laboratorio, sin historias reales.' }
        skills = @(@{ name = 'Valoración de enfermería'; category = 'ACADEMIC' }, @{ name = 'Comunicación con familias'; category = 'SOFT' }, @{ name = 'Registro clínico formativo'; category = 'PROFESSIONAL' })
        links = @{ linkedin = 'https://www.linkedin.com/in/mateo-quispe-muestra' }
    },
    @{
        first = 'Lucía'; last = 'Fernández'; email = 'lucia.fernandez.muestra@example.com'; phone = '+51 987 100 203'
        faculty = 'SALUD'; career = 'PSICO'; university = 'Pontificia Universidad Católica del Perú'
        region = 'Lima'; city = 'Lima'; district = 'Barranco'; layout = 'editorial'; status = 'ESTUDIANTE'; semester = '7'
        bio = 'Estudiante de Psicología. Muestro investigación formativa, cursos y talleres. No ofrezco terapia ni publico casos de consultantes.'
        interests = 'Psicología educativa y hábitos de estudio.'
        goals = 'Seguir la línea de orientación educativa después del bachillerato.'
        courses = 'Psicología del aprendizaje, Ética, Métodos de investigación'
        project = @{ name = 'Taller de hábitos de estudio'; description = 'Cuatro sesiones con compañeros de facultad sobre organización del tiempo. Participación voluntaria y sin expedientes.'; category = 'Educación'; role = 'Facilitación'; results = 'Guía de planificación semanal.'; tech = @('Dinámicas de grupo') }
        course = @{ name = 'Introducción a la psicometría'; institution = 'Pontificia Universidad Católica del Perú'; hours = 32; status = 'COMPLETADO'; description = 'Curso de lectura de instrumentos, sin aplicación clínica independiente.' }
        cert = @{ name = 'Ética en investigación con personas'; issuer = 'Comité de facultad'; description = 'Curso sobre consentimiento y anonimato.' }
        experience = @{ title = 'Apoyo en orientación vocacional'; organization = 'Bienestar universitario'; kind = 'FORMATIVE'; description = 'Charlas informativas supervisadas para escolares visitantes.' }
        publication = @{ title = 'Ensayo sobre hábitos de estudio'; venue = 'Revista de estudiantes'; kind = 'ARTICLE'; summary = 'Texto de curso sobre rutinas de lectura, sin datos personales de participantes.' }
        skills = @(@{ name = 'Entrevista formativa'; category = 'ACADEMIC' }, @{ name = 'Escucha activa'; category = 'SOFT' }, @{ name = 'Redacción académica'; category = 'PROFESSIONAL' })
        links = @{ linkedin = 'https://www.linkedin.com/in/lucia-fernandez-muestra' }
    },
    @{
        first = 'Andrés'; last = 'Salazar'; email = 'andres.salazar.muestra@example.com'; phone = '+51 987 100 204'
        faculty = 'LAW'; career = 'DERECHO'; university = 'Universidad de Lima'
        region = 'Lima'; city = 'Lima'; district = 'San Isidro'; layout = 'executive'; status = 'ESTUDIANTE'; semester = '9'
        bio = 'Estudiante de Derecho. Publico comentarios de normas y trabajos de aula. No patrocino casos ni me presento como abogado colegiado.'
        interests = 'Derecho constitucional y método de comentario normativo.'
        goals = 'Egresar y preparar el trabajo de suficiencia sobre acceso a información pública.'
        courses = 'Derecho constitucional, Argumentación jurídica, Derechos fundamentales'
        project = @{ name = 'Comentario a una ley publicada'; description = 'Análisis de una norma de acceso a la información, con fuentes abiertas y sin expedientes de clientes.'; category = 'Investigación jurídica'; role = 'Autor'; results = 'Informe de doce páginas para el curso.'; tech = @('Investigación normativa') }
        course = @{ name = 'Redacción de escritos'; institution = 'Universidad de Lima'; hours = 24; status = 'COMPLETADO'; description = 'Taller de estructura de escritos académicos.' }
        cert = @{ name = 'Curso de derechos fundamentales'; issuer = 'Facultad de Derecho'; description = 'Certificado de extensión universitaria.' }
        experience = @{ title = 'Consultorio jurídico estudiantil'; organization = 'Facultad de Derecho'; kind = 'FORMATIVE'; description = 'Apoyo supervisado en orientación general, sin firma de patrocinio.' }
        publication = @{ title = 'Nota sobre transparencia administrativa'; venue = 'Boletín de estudiantes'; kind = 'CASE_ANALYSIS'; summary = 'Comentario de una resolución pública, sin datos de partes privadas.' }
        skills = @(@{ name = 'Investigación normativa'; category = 'ACADEMIC' }, @{ name = 'Argumentación'; category = 'PROFESSIONAL' }, @{ name = 'Redacción clara'; category = 'SOFT' })
        links = @{ linkedin = 'https://www.linkedin.com/in/andres-salazar-muestra' }
    },
    @{
        first = 'Valeria'; last = 'Choque'; email = 'valeria.choque.muestra@example.com'; phone = '+51 987 100 205'
        faculty = 'ENG'; career = 'ING-SIS'; university = 'Universidad Nacional de Ingeniería'
        region = 'Lima'; city = 'Lima'; district = 'Rímac'; layout = 'tech'; status = 'ESTUDIANTE'; semester = '8'
        bio = 'Estudiante de Ingeniería de Sistemas. Reúno proyectos de software, el stack que uso y cursos de la carrera. El código de muestra está descrito como trabajo universitario.'
        interests = 'Aplicaciones web y diseño de bases de datos.'
        goals = 'Presentar un proyecto de fin de carrera sobre gestión académica.'
        courses = 'Base de datos, Ingeniería de software, Programación web'
        project = @{ name = 'Agenda académica del ciclo'; description = 'Aplicación de escritorio para ordenar cursos, entregas y horarios de un estudiante.'; category = 'Software'; role = 'Desarrollo'; results = 'Prototipo con inicio de sesión local y calendario.'; tech = @('Java'; 'PostgreSQL') }
        course = @{ name = 'Control de versiones con Git'; institution = 'Universidad Nacional de Ingeniería'; hours = 12; status = 'COMPLETADO'; description = 'Taller de ramas, revisiones y trabajo en equipo.' }
        cert = @{ name = 'Curso de SQL'; issuer = 'Laboratorio de bases de datos'; description = 'Certificado interno del laboratorio.' }
        experience = @{ title = 'Monitoría de programación'; organization = 'Escuela de Sistemas'; kind = 'ACADEMIC'; description = 'Horas de consulta para estudiantes de ciclos iniciales.' }
        publication = @{ title = 'Informe del prototipo de agenda'; venue = 'Curso de ingeniería de software'; kind = 'OTHER'; summary = 'Documento de requisitos y pruebas del prototipo.' }
        skills = @(@{ name = 'Java'; category = 'TECHNICAL' }, @{ name = 'PostgreSQL'; category = 'TOOL' }, @{ name = 'Modelado de datos'; category = 'ACADEMIC' })
        links = @{ linkedin = 'https://www.linkedin.com/in/valeria-choque-muestra'; github = 'https://github.com/valeria-choque-muestra' }
    },
    @{
        first = 'Diego'; last = 'Paredes'; email = 'diego.paredes.muestra@example.com'; phone = '+51 984 220 306'
        faculty = 'ARCH'; career = 'ARQ'; university = 'Universidad Nacional de San Antonio Abad del Cusco'
        region = 'Cusco'; city = 'Cusco'; district = 'Wanchaq'; layout = 'exhibit'; status = 'ESTUDIANTE'; semester = '7'
        bio = 'Estudiante de Arquitectura en Cusco. Muestro anteproyectos, el proceso de dibujo y las decisiones de emplazamiento. Las imágenes de obra todavía no están cargadas.'
        interests = 'Vivienda en ladera y espacio público de barrio.'
        goals = 'Desarrollar el taller de fin de carrera con un lote real ya autorizado por el curso.'
        courses = 'Taller de diseño, Construcción, Urbanismo'
        project = @{ name = 'Anteproyecto de vivienda en ladera'; description = 'Propuesta académica de una vivienda de dos niveles, con corte, planta y una nota de asoleamiento.'; category = 'Taller'; role = 'Autor del anteproyecto'; results = 'Láminas de planta, corte y memoria breve.'; tech = @('Dibujo arquitectónico'); architectureNote = 'Planta libre en el primer nivel y muro de contención hacia la ladera.' }
        course = @{ name = 'Representación arquitectónica'; institution = 'Universidad Nacional de San Antonio Abad del Cusco'; hours = 40; status = 'COMPLETADO'; description = 'Curso de planta, corte y axonometría.' }
        cert = @{ name = 'Taller de lectura de terreno'; issuer = 'Escuela de Arquitectura'; description = 'Salida de campo del curso de urbanismo.' }
        experience = @{ title = 'Apoyo en maquetas del taller'; organization = 'Taller vertical'; kind = 'ACADEMIC'; description = 'Armado de maquetas de estudio para la revisión de medio ciclo.' }
        publication = @{ title = 'Memoria del anteproyecto'; venue = 'Taller de diseño'; kind = 'OTHER'; summary = 'Texto que explica el emplazamiento y el programa de la vivienda.' }
        skills = @(@{ name = 'Dibujo de planos'; category = 'TECHNICAL' }, @{ name = 'Maquetas'; category = 'TOOL' }, @{ name = 'Lectura de sitio'; category = 'ACADEMIC' })
        links = @{ linkedin = 'https://www.linkedin.com/in/diego-paredes-muestra'; behance = 'https://www.behance.net/diego-paredes-muestra' }
    },
    @{
        first = 'Renata'; last = 'Vidal'; email = 'renata.vidal.muestra@example.com'; phone = '+51 987 100 207'
        faculty = 'ART'; career = 'DIS-GRAF'; university = 'Pontificia Universidad Católica del Perú'
        region = 'Lima'; city = 'Lima'; district = 'Surco'; layout = 'creative'; status = 'ESTUDIANTE'; semester = '6'
        bio = 'Estudiante de Diseño Gráfico. Reúno piezas de identidad, afiches de curso y el proceso de cada encargo universitario. No presento marcas comerciales ajenas como propias.'
        interests = 'Identidad visual y diseño editorial de corto formato.'
        goals = 'Armar un cuerpo de trabajo para la práctica preprofesional.'
        courses = 'Tipografía, Identidad visual, Editorial'
        project = @{ name = 'Identidad de una feria de curso'; description = 'Sistema gráfico para la feria de proyectos de la facultad: marca, paleta y afiche.'; category = 'Identidad'; role = 'Diseño'; results = 'Manual breve de uso de la marca del evento.'; tech = @('Tipografía'; 'Composición') }
        course = @{ name = 'Tipografía aplicada'; institution = 'Pontificia Universidad Católica del Perú'; hours = 28; status = 'COMPLETADO'; description = 'Ejercicios de jerarquía y retícula.' }
        cert = @{ name = 'Taller de afiche'; issuer = 'Escuela de diseño'; description = 'Certificado del taller intensivo de una semana.' }
        experience = @{ title = 'Apoyo gráfico del congreso de estudiantes'; organization = 'Centro de estudiantes'; kind = 'FORMATIVE'; description = 'Piezas para redes y señalética del evento interno.' }
        publication = @{ title = 'Memoria de la identidad de la feria'; venue = 'Curso de identidad visual'; kind = 'OTHER'; summary = 'Decisiones de color, tipo y aplicaciones.' }
        skills = @(@{ name = 'Composición'; category = 'TECHNICAL' }, @{ name = 'Tipografía'; category = 'ACADEMIC' }, @{ name = 'Illustrator'; category = 'TOOL' })
        links = @{ linkedin = 'https://www.linkedin.com/in/renata-vidal-muestra'; behance = 'https://www.behance.net/renata-vidal-muestra' }
    },
    @{
        first = 'Sofía'; last = 'Mendoza'; email = 'sofia.mendoza.muestra@example.com'; phone = '+51 987 100 208'
        faculty = 'BUS'; career = 'ADMIN'; university = 'Universidad del Pacífico'
        region = 'Lima'; city = 'Lima'; district = 'Jesús María'; layout = 'corporate'; status = 'ESTUDIANTE'; semester = '7'
        bio = 'Estudiante de Administración. Organizo trabajos de operaciones, cursos y una experiencia de apoyo en una feria universitaria. No publico cifras de empresas reales.'
        interests = 'Operaciones y organización de equipos pequeños.'
        goals = 'Llegar a la práctica con un portafolio de casos de curso.'
        courses = 'Operaciones, Costos, Comportamiento organizacional'
        project = @{ name = 'Plan de la feria de emprendimientos'; description = 'Organización de turnos, proveedores del campus y el recorrido de visitantes para una feria de la facultad.'; category = 'Operaciones'; role = 'Coordinación'; results = 'Cronograma y mapa de puestos.'; tech = @('Planificación') }
        course = @{ name = 'Hojas de cálculo para gestión'; institution = 'Universidad del Pacífico'; hours = 18; status = 'COMPLETADO'; description = 'Curso de tablas y gráficos para informes de curso.' }
        cert = @{ name = 'Taller de trabajo en equipo'; issuer = 'Bienestar universitario'; description = 'Certificado de un taller de coordinación.' }
        experience = @{ title = 'Apoyo en admisiones de un evento'; organization = 'Facultad de Administración'; kind = 'FORMATIVE'; description = 'Registro de asistentes y entrega de materiales del congreso interno.' }
        publication = @{ title = 'Informe de la feria'; venue = 'Curso de operaciones'; kind = 'OTHER'; summary = 'Qué se planificó, qué ocurrió y qué se ajustaría.' }
        skills = @(@{ name = 'Planificación'; category = 'PROFESSIONAL' }, @{ name = 'Hojas de cálculo'; category = 'TOOL' }, @{ name = 'Coordinación'; category = 'SOFT' })
        links = @{ linkedin = 'https://www.linkedin.com/in/sofia-mendoza-muestra' }
    },
    @{
        first = 'Hugo'; last = 'Castillo'; email = 'hugo.castillo.muestra@example.com'; phone = '+51 987 100 209'
        faculty = 'ART'; career = 'DIS-INT'; university = 'Universidad Peruana de Ciencias Aplicadas'
        region = 'Lima'; city = 'Lima'; district = 'Miraflores'; layout = 'interior'; status = 'ESTUDIANTE'; semester = '5'
        bio = 'Estudiante de Diseño de Interiores. Muestro una propuesta de aula, el criterio de luz y la distribución del mobiliario. Es un encargo de taller, no una obra construida.'
        interests = 'Espacios de estudio y luz natural.'
        goals = 'Completar el taller con un segundo espacio, esta vez comercial y también académico.'
        courses = 'Taller de interiores, Color, Materiales'
        project = @{ name = 'Aula de lectura para cuarenta personas'; description = 'Redistribución de un aula existente del campus: mesas, estantería y luz de tarde.'; category = 'Interiores'; role = 'Propuesta'; results = 'Planta de mobiliario y una memoria de materiales.'; tech = @('Distribución'; 'Iluminación'); architectureNote = 'Las mesas largas quedan perpendiculares a la ventana para reducir el reflejo en las lecturas.' }
        course = @{ name = 'Materiales de interior'; institution = 'Universidad Peruana de Ciencias Aplicadas'; hours = 24; status = 'COMPLETADO'; description = 'Reconocimiento de maderas, textiles y pinturas de uso interior.' }
        cert = @{ name = 'Taller de iluminación'; issuer = 'Escuela de diseño'; description = 'Ejercicio de temperatura de color en un aula.' }
        experience = @{ title = 'Apoyo en el montaje de la exposición de taller'; organization = 'Facultad de Diseño'; kind = 'FORMATIVE'; description = 'Colocación de láminas y maquetas de fin de ciclo.' }
        publication = @{ title = 'Memoria del aula de lectura'; venue = 'Taller de interiores'; kind = 'OTHER'; summary = 'Criterios de circulación, luz y mobiliario.' }
        skills = @(@{ name = 'Distribución de mobiliario'; category = 'TECHNICAL' }, @{ name = 'Color'; category = 'ACADEMIC' }, @{ name = 'Dibujo de planta'; category = 'TOOL' })
        links = @{ linkedin = 'https://www.linkedin.com/in/hugo-castillo-muestra'; behance = 'https://www.behance.net/hugo-castillo-muestra' }
    },
    @{
        first = 'Elena'; last = 'Navarro'; email = 'elena.navarro.muestra@example.com'; phone = '+51 944 330 410'
        faculty = 'COMM'; career = 'PERIOD'; university = 'Universidad Nacional de Trujillo'
        region = 'La Libertad'; city = 'Trujillo'; district = 'Trujillo'; layout = 'screen'; status = 'ESTUDIANTE'; semester = '6'
        bio = 'Estudiante de Periodismo en Trujillo. Publico reportajes de curso, la edición y las fuentes abiertas que usé. No reproduzco entrevistas sin el acuerdo que ya pidió el taller.'
        interests = 'Periodismo local y edición de piezas breves.'
        goals = 'Armar una carpeta de tres reportajes antes de la práctica.'
        courses = 'Reportería, Ética periodística, Edición'
        project = @{ name = 'Reportaje sobre el transporte al campus'; description = 'Pieza sobre los tiempos de viaje de estudiantes, con entrevistas acordadas y sin publicar teléfonos privados.'; category = 'Reportaje'; role = 'Reportería y edición'; results = 'Texto de 900 palabras y un esquema de fuentes.'; tech = @('Entrevista'; 'Edición') }
        course = @{ name = 'Ética y fuentes'; institution = 'Universidad Nacional de Trujillo'; hours = 20; status = 'COMPLETADO'; description = 'Curso sobre cita de fuentes y protección de personas entrevistadas.' }
        cert = @{ name = 'Taller de crónica breve'; issuer = 'Escuela de periodismo'; description = 'Certificado del taller de escritura de mitad de ciclo.' }
        experience = @{ title = 'Pasantía en el medio de la universidad'; organization = 'Medio universitario'; kind = 'PREPROFESSIONAL'; description = 'Edición de notas cortas del calendario académico.' }
        publication = @{ title = 'El camino al campus'; venue = 'Medio universitario'; kind = 'ARTICLE'; summary = 'Reportaje de curso sobre tiempos de transporte, con fuentes identificadas solo si aceptaron el nombre.' }
        skills = @(@{ name = 'Entrevista'; category = 'PROFESSIONAL' }, @{ name = 'Edición'; category = 'TECHNICAL' }, @{ name = 'Ética de fuentes'; category = 'ACADEMIC' })
        links = @{ linkedin = 'https://www.linkedin.com/in/elena-navarro-muestra'; website = 'https://example.com/elena-navarro-muestra' }
    }
)

$created = @()
foreach ($person in $people) {
    $auth = $null
    try {
        $auth = Send-Api POST "$api/auth/login" @{ email = $person.email; password = $password }
    } catch {
        $auth = Send-Api POST "$api/auth/register" @{
            firstName = $person.first; lastName = $person.last; email = $person.email
            password = $password; confirmPassword = $password; countryCode = 'PE'; acceptTerms = $true
        }
    }
    $token = $auth.accessToken
    Send-Api PUT "$api/profiles/me" @{
        firstName = $person.first; lastName = $person.last
        bio = $person.bio; bioEn = $null; birthDate = '2003-04-12'; showBirthDate = $false
        countryId = $peru.id; regionId = $null; provinceId = $null; cityId = $null; districtId = $null
        customRegion = $person.region; customProvince = $person.city; customCity = $person.city; customDistrict = $person.district
        address = $null; phone = $person.phone; professionalEmail = $person.email
        linkedin = $person.links.linkedin; github = $person.links.github; gitlab = $null
        behance = $person.links.behance; dribbble = $null; orcid = $null; googleScholar = $null; website = $person.links.website
        otherLinks = @()
    } $token | Out-Null
    $already = @(Send-Api GET "$api/projects" $null $token)
    if ($already.Count -eq 0) {
    Send-Api PUT "$api/academic/me" @{
        universityId = $universities[$person.university]; universityName = $person.university
        facultyId = $faculties[$person.faculty]; careerId = $careers[$person.career]; specializationId = $null
        semester = $person.semester; admissionYear = 2022; expectedGraduation = '2027-12-15'; status = $person.status
        relevantCourses = $person.courses; interests = $person.interests; professionalGoals = $person.goals
        licenseDeclared = $false; careerName = $null; careerKind = 'UNIVERSITY'
    } $token | Out-Null
    $project = $person.project
    Send-Api POST "$api/projects" @{
        name = $project.name; description = $project.description; descriptionEn = $null; category = $project.category
        startDate = '2025-03-01'; endDate = '2025-07-15'; roleName = $project.role; results = $project.results
        architectureNote = $project.architectureNote; links = @{}; participants = @(); technologies = $project.tech
        publicationStatus = 'PUBLISHED'; coverDocumentId = $null
    } $token | Out-Null
    Send-Api POST "$api/courses" @{
        name = $person.course.name; institution = $person.course.institution
        startDate = '2025-04-01'; endDate = '2025-05-01'; hours = $person.course.hours; status = $person.course.status
        description = $person.course.description; descriptionEn = $null; certificateDocumentId = $null
    } $token | Out-Null
    Send-Api POST "$api/certifications" @{
        name = $person.cert.name; issuer = $person.cert.issuer; issuedOn = '2025-06-01'; expiresOn = $null
        credentialId = $null; description = $person.cert.description; descriptionEn = $null; documentId = $null
    } $token | Out-Null
    Send-Api POST "$api/experiences" @{
        title = $person.experience.title; organization = $person.experience.organization; kind = $person.experience.kind
        startDate = '2025-08-01'; endDate = '2025-11-01'; description = $person.experience.description; descriptionEn = $null
    } $token | Out-Null
    Send-Api POST "$api/publications" @{
        title = $person.publication.title; authors = "$($person.first) $($person.last)"; venue = $person.publication.venue
        publishedOn = '2025-11-01'; url = $null; kind = $person.publication.kind
        summary = $person.publication.summary; summaryEn = $null
    } $token | Out-Null
    foreach ($skill in $person.skills) {
        Send-Api POST "$api/skills" @{ name = $skill.name; category = $skill.category; note = 'Declarada por el estudiante.' } $token | Out-Null
    }
    }
    Send-Api POST "$api/portfolios/me/template" @{ templateId = $templates[$person.layout] } $token | Out-Null
    $editor = Send-Api GET "$api/portfolios/me" $null $token
    Send-Api PUT "$api/portfolios/me" @{
        version = $editor.portfolio.version
        title = "Portafolio de $($person.first) $($person.last)"
        theme = $null; bilingual = $false; whatsappEnabled = $true; showEmail = $true; showPhone = $true
        analyticsEnabled = $true; recruiterViewEnabled = $true; showcaseOptIn = $true
        seoTitle = "$($person.first) $($person.last)"
        seoDescription = $person.bio.Substring(0, [Math]::Min(180, $person.bio.Length))
    } $token | Out-Null
    $dash = Send-Api GET "$api/dashboard" $null $token
    $created += [pscustomobject]@{ nombre = "$($person.first) $($person.last)"; correo = $person.email; sitio = $dash.publicUrl; diseno = $person.layout }
}

$created | Format-Table -AutoSize | Out-String | Write-Output
$created | ConvertTo-Json | Set-Content -Encoding utf8 "$env:TEMP\muestra-usuarios.json"
