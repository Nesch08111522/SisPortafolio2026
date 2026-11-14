package com.portfoliohub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.portfoliohub.common.ApiException;
import com.portfoliohub.common.Texts;
import com.portfoliohub.config.AppProperties;
import com.portfoliohub.domain.Certification;
import com.portfoliohub.domain.Course;
import com.portfoliohub.domain.CourseStatus;
import com.portfoliohub.domain.DocumentCategory;
import com.portfoliohub.domain.DocumentExtraction;
import com.portfoliohub.domain.DocumentVisibility;
import com.portfoliohub.domain.ExtractionStatus;
import com.portfoliohub.domain.PersonalProfile;
import com.portfoliohub.domain.ScanStatus;
import com.portfoliohub.domain.StoredDocument;
import com.portfoliohub.domain.VerificationStatus;
import com.portfoliohub.repo.CertificationRepository;
import com.portfoliohub.repo.CourseRepository;
import com.portfoliohub.repo.DocumentExtractionRepository;
import com.portfoliohub.repo.PersonalProfileRepository;
import com.portfoliohub.repo.StoredDocumentRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentService {

    private static final Set<String> EXTENSIONS = Set.of("pdf", "png", "jpg", "jpeg", "webp", "doc", "docx", "pptx");
    private static final Pattern HOURS = Pattern.compile("(?i)(\\d{1,4})\\s*(horas|hours)\\b");
    private static final Pattern DATE = Pattern.compile("\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})\\b");
    private static final Pattern INSTITUTION = Pattern.compile("(?i).*(universidad|instituto|institute|coursera|edx|google|microsoft|ibm|aws|oracle|cisco).*");

    private final StoredDocumentRepository documents;
    private final DocumentExtractionRepository extractions;
    private final PersonalProfileRepository personalProfiles;
    private final CourseRepository courses;
    private final CertificationRepository certifications;
    private final AppProperties properties;
    private final ObjectMapper mapper;

    public DocumentService(
            StoredDocumentRepository documents,
            DocumentExtractionRepository extractions,
            PersonalProfileRepository personalProfiles,
            CourseRepository courses,
            CertificationRepository certifications,
            AppProperties properties,
            ObjectMapper mapper
    ) {
        this.documents = documents;
        this.extractions = extractions;
        this.personalProfiles = personalProfiles;
        this.courses = courses;
        this.certifications = certifications;
        this.properties = properties;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<StoredDocument> list(UUID userId) {
        return documents.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public StoredDocument upload(UUID userId, MultipartFile file, DocumentCategory category, boolean visibleOnPortfolio, boolean profilePhoto) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("FILE_REQUIRED", "Selecciona un archivo.");
        }
        if (file.getSize() > properties.getStorage().getMaxBytes()) {
            throw ApiException.badRequest("FILE_TOO_LARGE", "El archivo supera el límite de 15 MB.");
        }
        String original = safeName(file.getOriginalFilename());
        String extension = extension(original);
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException exception) {
            throw ApiException.badRequest("FILE_UNREADABLE", "No se pudo leer el archivo.");
        }
        if (!matchesSignature(extension, bytes)) {
            throw ApiException.badRequest("FILE_TYPE", "El contenido del archivo no coincide con su extensión.");
        }
        ScanResult scan = scan(bytes);
        if (scan.status() == ScanStatus.INFECTED) {
            throw ApiException.badRequest("FILE_REJECTED", "El archivo fue rechazado por el análisis antimalware.");
        }
        if (scan.status() == ScanStatus.ERROR) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "SCAN_UNAVAILABLE", "El antivirus está configurado pero no respondió. El archivo no se guardó.");
        }
        String key = userId + "/" + UUID.randomUUID() + "." + extension;
        Path path = resolve(key);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR", "No se pudo guardar el archivo.");
        }
        StoredDocument document = new StoredDocument();
        document.setUserId(userId);
        document.setOriginalName(original);
        document.setStorageKey(key);
        document.setMimeType(mime(extension));
        document.setSizeBytes(bytes.length);
        document.setCategory(category == null ? guessCategory(original, "") : category);
        document.setVisibility(visibleOnPortfolio || profilePhoto ? DocumentVisibility.PUBLIC : DocumentVisibility.PRIVATE);
        document.setChecksum(Texts.sha256(bytes));
        document.setScanStatus(scan.status());
        document.setScanDetail(scan.detail());
        documents.save(document);
        if (profilePhoto) {
            PersonalProfile profile = personalProfiles.findById(userId).orElseThrow();
            profile.setPhotoDocumentId(document.getId());
        }
        return document;
    }

    @Transactional
    public StoredDocument visibility(UUID userId, UUID id, DocumentVisibility visibility) {
        StoredDocument document = documents.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("No encontramos ese documento."));
        document.setVisibility(visibility);
        return document;
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        StoredDocument document = documents.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("No encontramos ese documento."));
        document.setDeletedAt(Instant.now());
        try {
            Files.deleteIfExists(resolve(document.getStorageKey()));
        } catch (IOException ignored) {
            document.setScanDetail("No se pudo borrar el archivo físico.");
        }
    }

    @Transactional(readOnly = true)
    public StoredFile read(UUID userId, UUID id) {
        StoredDocument document = documents.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("No encontramos ese documento."));
        return readStored(document);
    }

    @Transactional(readOnly = true)
    public StoredFile readPublic(UUID id) {
        StoredDocument document = documents.findById(id)
                .filter(item -> item.getVisibility() == DocumentVisibility.PUBLIC)
                .orElseThrow(() -> ApiException.notFound("No encontramos ese archivo."));
        return readStored(document);
    }

    @Transactional
    public Map<String, Object> process(UUID userId, UUID id) {
        StoredDocument document = documents.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("No encontramos ese documento."));
        String text = extractText(document);
        ObjectNode suggestion = mapper.createObjectNode();
        List<String> notes = new ArrayList<>();
        suggestion.put("fileName", document.getOriginalName());
        if (text.isBlank()) {
            notes.add("No se extrajo texto. Si es un documento escaneado, instala Tesseract o completa los datos a mano.");
        } else {
            suggestion.put("textSample", text.substring(0, Math.min(text.length(), 1500)));
            Matcher hours = HOURS.matcher(text);
            if (hours.find()) {
                suggestion.put("suggestedHours", Integer.parseInt(hours.group(1)));
            } else {
                notes.add("No se encontró una duración en horas.");
            }
            Matcher date = DATE.matcher(text);
            if (date.find()) {
                suggestion.put("suggestedDateText", date.group(1));
            } else {
                notes.add("No se encontró una fecha explícita.");
            }
            String institution = firstInstitution(text);
            if (institution != null) {
                suggestion.put("suggestedInstitution", institution);
            } else {
                notes.add("No se identificó una institución emisora.");
            }
        }
        suggestion.put("suggestedCategory", guessCategory(document.getOriginalName(), text).name());
        suggestion.put("notes", mapper.valueToTree(notes));
        suggestion.put("authenticity", "La extracción es una sugerencia. No confirma que el documento sea auténtico.");
        DocumentExtraction extraction = new DocumentExtraction();
        extraction.setDocumentId(document.getId());
        extraction.setExtractedJson(suggestion);
        extraction.setStatus(ExtractionStatus.PENDING_REVIEW);
        extractions.save(extraction);
        return Map.of("id", extraction.getId(), "status", extraction.getStatus(), "suggestion", suggestion);
    }

    @Transactional
    public Map<String, Object> confirm(UUID userId, UUID documentId, UUID extractionId, ConfirmInput input) {
        StoredDocument document = documents.findByIdAndUserId(documentId, userId).orElseThrow(() -> ApiException.notFound("No encontramos ese documento."));
        DocumentExtraction extraction = extractions.findByIdAndDocumentId(extractionId, documentId)
                .orElseThrow(() -> ApiException.notFound("No encontramos esa extracción."));
        String name = Texts.limit(input.name(), 200);
        if (name == null) {
            throw ApiException.badRequest("NAME_REQUIRED", "Confirma al menos el nombre antes de guardar.");
        }
        ObjectNode confirmed = mapper.createObjectNode();
        confirmed.put("name", name);
        String institution = Texts.limit(input.institution(), 200);
        if (institution != null) {
            confirmed.put("institution", institution);
        }
        if (input.date() != null) {
            confirmed.put("date", input.date().toString());
        }
        if (input.hours() != null) {
            confirmed.put("hours", input.hours());
        }
        String description = Texts.limit(input.description(), 2000);
        if (description != null) {
            confirmed.put("description", description);
        }
        confirmed.put("target", input.target() == null ? "NONE" : input.target());
        extraction.setConfirmedJson(confirmed);
        extraction.setConfirmedAt(Instant.now());
        extraction.setStatus(ExtractionStatus.CONFIRMED);
        UUID createdId = null;
        if ("COURSE".equals(input.target())) {
            Course course = new Course();
            course.setUserId(userId);
            course.setName(name);
            course.setInstitution(Texts.limit(input.institution(), 200));
            course.setEndDate(input.date());
            course.setHours(input.hours());
            course.setStatus(CourseStatus.COMPLETADO);
            course.setDescription(Texts.limit(input.description(), 2000));
            course.setCertificateDocumentId(document.getId());
            createdId = courses.save(course).getId();
        } else if ("CERTIFICATION".equals(input.target())) {
            Certification certification = new Certification();
            certification.setUserId(userId);
            certification.setName(name);
            certification.setIssuer(Texts.limit(input.institution(), 200));
            certification.setIssuedOn(input.date());
            certification.setDescription(Texts.limit(input.description(), 2000));
            certification.setDocumentId(document.getId());
            certification.setVerificationStatus(VerificationStatus.USER_CONFIRMED);
            createdId = certifications.save(certification).getId();
        }
        return Map.of(
                "status", extraction.getStatus(),
                "createdId", createdId == null ? "" : createdId,
                "notice", "Guardamos solo los datos que confirmaste. Esto no autentica el documento."
        );
    }

    private StoredFile readStored(StoredDocument document) {
        try {
            return new StoredFile(document, Files.readAllBytes(resolve(document.getStorageKey())));
        } catch (IOException exception) {
            throw ApiException.notFound("El archivo ya no está disponible.");
        }
    }

    private String extractText(StoredDocument document) {
        try {
            byte[] bytes = Files.readAllBytes(resolve(document.getStorageKey()));
            String extension = extension(document.getStorageKey());
            return switch (extension) {
                case "pdf" -> pdfText(bytes);
                case "docx" -> docxText(bytes);
                case "doc" -> docText(bytes);
                case "pptx" -> pptxText(bytes);
                default -> "";
            };
        } catch (Exception exception) {
            return "";
        }
    }

    private String pdfText(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setEndPage(Math.min(document.getNumberOfPages(), 20));
            return stripper.getText(document);
        }
    }

    private String docxText(byte[] bytes) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes));
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String docText(byte[] bytes) throws IOException {
        try (HWPFDocument document = new HWPFDocument(new ByteArrayInputStream(bytes));
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String pptxText(byte[] bytes) throws IOException {
        try (XMLSlideShow show = new XMLSlideShow(new ByteArrayInputStream(bytes))) {
            StringBuilder text = new StringBuilder();
            show.getSlides().forEach(slide -> {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        text.append(textShape.getText()).append('\n');
                    }
                }
            });
            return text.toString();
        }
    }

    private String firstInstitution(String text) {
        for (String line : text.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.length() >= 6 && trimmed.length() <= 180 && INSTITUTION.matcher(trimmed).matches()) {
                return trimmed;
            }
        }
        return null;
    }

    private DocumentCategory guessCategory(String name, String text) {
        String probe = (name + " " + text).toLowerCase(Locale.ROOT);
        if (probe.contains("curriculum") || probe.contains("currículum") || probe.contains("cv")) {
            return DocumentCategory.CV;
        }
        if (probe.contains("diploma")) {
            return DocumentCategory.DIPLOMA;
        }
        if (probe.contains("constancia")) {
            return DocumentCategory.CONSTANCIA;
        }
        if (probe.contains("certific")) {
            return DocumentCategory.CERTIFICATE;
        }
        if (probe.contains("tesis") || probe.contains("investig")) {
            return DocumentCategory.RESEARCH;
        }
        return DocumentCategory.OTHER;
    }

    private ScanResult scan(byte[] bytes) {
        String host = properties.getClamav().getHost();
        if (host == null || host.isBlank()) {
            return new ScanResult(ScanStatus.SKIPPED, "Análisis antimalware no configurado.");
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, properties.getClamav().getPort()), 3000);
            socket.setSoTimeout(15000);
            OutputStream output = socket.getOutputStream();
            output.write("zINSTREAM\0".getBytes(StandardCharsets.US_ASCII));
            output.write(ByteBuffer.allocate(4).putInt(bytes.length).array());
            output.write(bytes);
            output.write(new byte[]{0, 0, 0, 0});
            output.flush();
            String response = new String(socket.getInputStream().readAllBytes(), StandardCharsets.US_ASCII);
            if (response.contains("FOUND")) {
                return new ScanResult(ScanStatus.INFECTED, "Se detectó contenido malicioso.");
            }
            if (response.contains("OK")) {
                return new ScanResult(ScanStatus.CLEAN, "Sin detecciones.");
            }
            return new ScanResult(ScanStatus.ERROR, "Respuesta de antivirus no reconocida.");
        } catch (IOException exception) {
            return new ScanResult(ScanStatus.ERROR, "No se pudo contactar al antivirus.");
        }
    }

    private boolean matchesSignature(String extension, byte[] bytes) {
        if (bytes.length < 4) {
            return false;
        }
        return switch (extension) {
            case "pdf" -> starts(bytes, new byte[]{0x25, 0x50, 0x44, 0x46});
            case "png" -> starts(bytes, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});
            case "jpg", "jpeg" -> starts(bytes, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
            case "webp" -> bytes.length > 12 && starts(bytes, new byte[]{0x52, 0x49, 0x46, 0x46}) && bytes[8] == 0x57 && bytes[9] == 0x45;
            case "doc" -> starts(bytes, new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0});
            case "docx", "pptx" -> starts(bytes, new byte[]{0x50, 0x4B, 0x03, 0x04});
            default -> false;
        };
    }

    private boolean starts(byte[] bytes, byte[] signature) {
        for (int index = 0; index < signature.length; index++) {
            if (bytes[index] != signature[index]) {
                return false;
            }
        }
        return true;
    }

    private String extension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            throw ApiException.badRequest("FILE_TYPE", "El archivo necesita una extensión permitida: PDF, PNG, JPG, WEBP, DOC, DOCX o PPTX.");
        }
        String extension = name.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!EXTENSIONS.contains(extension)) {
            throw ApiException.badRequest("FILE_TYPE", "Ese tipo de archivo no está permitido.");
        }
        return extension;
    }

    private String safeName(String original) {
        String name = original == null ? "archivo" : Path.of(original).getFileName().toString();
        name = name.replaceAll("[\\r\\n\"]", "");
        if (name.isBlank()) {
            name = "archivo";
        }
        return name.length() > 200 ? name.substring(name.length() - 200) : name;
    }

    private String mime(String extension) {
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default -> "application/octet-stream";
        };
    }

    private Path resolve(String key) {
        Path root = Path.of(properties.getStorage().getPath()).toAbsolutePath().normalize();
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw ApiException.badRequest("INVALID_PATH", "La ruta del archivo no es válida.");
        }
        return target;
    }

    public record StoredFile(StoredDocument document, byte[] bytes) {
    }

    public record ConfirmInput(String target, String name, String institution, LocalDate date, Integer hours, String description) {
    }

    private record ScanResult(ScanStatus status, String detail) {
    }
}
