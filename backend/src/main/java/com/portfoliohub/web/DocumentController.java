package com.portfoliohub.web;

import com.portfoliohub.domain.DocumentCategory;
import com.portfoliohub.domain.DocumentVisibility;
import com.portfoliohub.security.CurrentUser;
import com.portfoliohub.service.DocumentService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documents;

    public DocumentController(DocumentService documents) {
        this.documents = documents;
    }

    @GetMapping
    public Object list() {
        return documents.list(CurrentUser.id());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Object upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) DocumentCategory category,
            @RequestParam(defaultValue = "false") boolean visibleOnPortfolio,
            @RequestParam(defaultValue = "false") boolean profilePhoto
    ) {
        return documents.upload(CurrentUser.id(), file, category, visibleOnPortfolio, profilePhoto);
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<byte[]> content(@PathVariable UUID id) {
        DocumentService.StoredFile file = documents.read(CurrentUser.id(), id);
        return fileResponse(file, false);
    }

    @PostMapping("/{id}/process")
    public Map<String, Object> process(@PathVariable UUID id) {
        return documents.process(CurrentUser.id(), id);
    }

    @PostMapping("/{id}/extractions/{extractionId}/confirm")
    public Map<String, Object> confirm(@PathVariable UUID id, @PathVariable UUID extractionId, @RequestBody ConfirmBody body) {
        return documents.confirm(CurrentUser.id(), id, extractionId, new DocumentService.ConfirmInput(
                body.target(), body.name(), body.institution(), body.date(), body.hours(), body.description()
        ));
    }

    @PatchMapping("/{id}/visibility")
    public Object visibility(@PathVariable UUID id, @RequestBody Map<String, DocumentVisibility> body) {
        return documents.visibility(CurrentUser.id(), id, body.get("visibility"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        documents.delete(CurrentUser.id(), id);
    }

    static ResponseEntity<byte[]> fileResponse(DocumentService.StoredFile file, boolean inlineImages) {
        boolean image = file.document().getMimeType().startsWith("image/");
        ContentDisposition disposition = ContentDisposition.builder(inlineImages && image ? "inline" : "attachment")
                .filename(file.document().getOriginalName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(file.document().getMimeType()))
                .body(file.bytes());
    }

    public record ConfirmBody(String target, String name, String institution, LocalDate date, Integer hours, String description) {
    }
}
