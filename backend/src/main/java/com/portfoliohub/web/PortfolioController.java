package com.portfoliohub.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.portfoliohub.common.ApiException;
import com.portfoliohub.security.ClientIp;
import com.portfoliohub.security.CurrentUser;
import com.portfoliohub.service.PortfolioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PortfolioController {

    private final PortfolioService portfolios;
    private final ClientIp clientIp;

    public PortfolioController(PortfolioService portfolios, ClientIp clientIp) {
        this.portfolios = portfolios;
        this.clientIp = clientIp;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return portfolios.dashboard(CurrentUser.id());
    }

    @GetMapping("/portfolios/me")
    public Map<String, Object> mine() {
        return portfolios.editor(CurrentUser.id());
    }

    @GetMapping("/portfolios/me/preview")
    public Map<String, Object> preview() {
        return portfolios.preview(CurrentUser.id());
    }

    @PutMapping("/portfolios/me")
    public Map<String, Object> settings(@RequestBody PortfolioService.SettingsUpdate body) {
        return portfolios.updateSettings(CurrentUser.id(), body);
    }

    @PutMapping("/portfolios/me/sections")
    public Map<String, Object> sections(@RequestBody SectionsBody body) {
        return portfolios.updateSections(CurrentUser.id(), body.version(), body.sections().stream()
                .map(item -> new PortfolioService.SectionUpdate(item.key(), item.title(), item.visible(), item.sortOrder(), item.content()))
                .toList());
    }

    @PostMapping("/portfolios/me/template")
    public Map<String, Object> template(@RequestBody Map<String, UUID> body) {
        return portfolios.applyTemplate(CurrentUser.id(), body.get("templateId"));
    }

    @DeleteMapping("/portfolios/me/sections/{key}")
    public Map<String, Object> deleteSection(@PathVariable String key) {
        return portfolios.deleteSection(CurrentUser.id(), key);
    }

    @PostMapping("/portfolios/me/versions")
    public Object version(@RequestBody(required = false) Map<String, String> body) {
        return portfolios.saveVersion(CurrentUser.id(), body == null ? null : body.get("label"));
    }

    @PostMapping("/portfolios/me/versions/{id}/restore")
    public Map<String, Object> restore(@PathVariable UUID id) {
        return portfolios.restore(CurrentUser.id(), id);
    }

    @GetMapping("/portfolios/slug-availability")
    public Map<String, Object> slug(@RequestParam String slug) {
        return portfolios.slugAvailability(CurrentUser.id(), slug);
    }

    @PostMapping("/portfolios/me/publish")
    public Map<String, Object> publish(@RequestBody PublishBody body, HttpServletRequest request) {
        return portfolios.publish(CurrentUser.id(), body.slug(), body.acceptPublication(), body.seoTitle(), body.seoDescription(), clientIp.resolve(request));
    }

    @PostMapping("/portfolios/me/unpublish")
    public Map<String, Object> unpublish(@RequestBody(required = false) Map<String, String> body, HttpServletRequest request) {
        return portfolios.unpublish(CurrentUser.id(), body == null ? null : body.get("reason"), clientIp.resolve(request));
    }

    @PostMapping("/portfolios/me/domains")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public Object domain(@RequestBody Map<String, String> body) {
        return portfolios.registerDomain(CurrentUser.id(), body.get("domain"));
    }

    @GetMapping(value = "/portfolios/me/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr() {
        var portfolio = portfolios.owned(CurrentUser.id());
        String url = portfolios.publicUrl(portfolio);
        if (url == null || portfolio.getSlug() == null) {
            throw ApiException.badRequest("SLUG_REQUIRED", "Define un identificador antes de generar el código QR.");
        }
        try {
            var matrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 360, 360);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(output.toByteArray());
        } catch (Exception exception) {
            throw ApiException.badRequest("QR_ERROR", "No se pudo generar el código QR.");
        }
    }

    public record SectionsBody(long version, List<SectionBody> sections) {
    }

    public record SectionBody(String key, String title, boolean visible, int sortOrder, JsonNode content) {
    }

    public record PublishBody(String slug, boolean acceptPublication, String seoTitle, String seoDescription) {
    }
}
