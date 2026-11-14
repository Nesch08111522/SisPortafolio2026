# Portfolio Hub

Plataforma para que estudiantes y egresados construyan un sitio profesional según su carrera, sin programar. El contenido vive en PostgreSQL. Los archivos se guardan en disco y, por defecto, permanecen privados.

## Arranque local

Requisitos: Java 17, Node.js 22 y npm 11. Si no tienes PostgreSQL, la API inicia uno embebido y guarda los datos en `backend/data/postgres`.

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
cd backend
& "$env:TEMP\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
```

En otra terminal:

```powershell
cd frontend
npx ng serve
```

Abre `http://localhost:4200`. La API queda en `http://localhost:8080` y su contrato en `http://localhost:8080/swagger-ui.html`.

En local, el correo no sale por SMTP. El registro devuelve `devVerificationUrl` para confirmar la cuenta. La cuenta de administración inicial es `admin@portfoliohub.local` / `ChangeMe-Admin-17`. Cámbiala antes de compartir el entorno.

## Recorrido

1. Crear la cuenta, confirmar el correo e iniciar sesión.
2. Elegir universidad, facultad, carrera, especialización y estado académico.
3. Completar perfil, proyectos, cursos, certificaciones y habilidades. Las habilidades no usan porcentajes.
4. Subir documentos. La extracción solo propone datos; hay que confirmarlos.
5. Elegir uno de los diez diseños de la carrera y editar secciones. Cambiar de diseño conserva el texto de las secciones compatibles.
6. Publicar solo con la confirmación explícita. La dirección pública es `/p/{slug}`.

Un borrador o un sitio despublicado responde 404. Medicina, derecho, psicología y veterinaria muestran un aviso: el portafolio no acredita habilitación profesional.

## Producción

Compila el backend con `mvn package` y levanta PostgreSQL con Docker:

```powershell
docker compose up --build
```

Define los secretos de `.env.example`. El perfil `prod` no expone enlaces de verificación ni la documentación OpenAPI. Los dominios propios se registran como interés; esta versión no enruta tráfico hacia ellos.

## Pruebas

```powershell
cd backend
mvn test
cd ../frontend
npx ng test --watch=false
```
