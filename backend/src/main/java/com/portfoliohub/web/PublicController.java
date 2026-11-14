package com.portfoliohub.web;

import com.portfoliohub.repo.PortfolioRepository;
import com.portfoliohub.domain.PortfolioStatus;
import com.portfoliohub.service.DocumentService;
import com.portfoliohub.service.PortfolioService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    private final PortfolioService portfolios;
    private final DocumentService documents;
    private final PortfolioRepository portfolioRepository;
    private final com.portfoliohub.config.AppProperties properties;

    public PublicController(PortfolioService portfolios, DocumentService documents, PortfolioRepository portfolioRepository, com.portfoliohub.config.AppProperties properties) {
        this.portfolios = portfolios;
        this.documents = documents;
        this.portfolioRepository = portfolioRepository;
        this.properties = properties;
    }

    @GetMapping("/examples")
    public Object examples() {
        return portfolios.examples();
    }

    @GetMapping("/{slug}")
    public Map<String, Object> portfolio(@PathVariable String slug) {
        if ("examples".equals(slug) || "files".equals(slug) || "sitemap.xml".equals(slug)) {
            return Map.of();
        }
        return portfolios.publicView(slug);
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<byte[]> file(@PathVariable UUID id) {
        return DocumentController.fileResponse(documents.readPublic(id), true);
    }

    @PostMapping("/{slug}/visits")
    public ResponseEntity<Void> visit(@PathVariable String slug, @RequestBody VisitBody body, @RequestHeader(value = "Referer", required = false) String referrer) {
        portfolios.visit(slug, body.consent(), body.path(), body.device(), referrer);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{slug}/contact")
    public Map<String, String> contact(@PathVariable String slug, @RequestBody ContactBody body) {
        portfolios.contact(slug, body.name(), body.email(), body.message(), body.companyWebsite());
        return Map.of("message", "Mensaje enviado.");
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        portfolioRepository.findByStatusAndShowcaseOptInTrue(PortfolioStatus.PUBLISHED, PageRequest.of(0, 1));
        portfolioRepository.findAll().stream()
                .filter(item -> item.getStatus() == PortfolioStatus.PUBLISHED && item.getSlug() != null)
                .forEach(item -> xml.append("<url><loc>")
                        .append(escape(properties.getPublicBaseUrl() + "/p/" + item.getSlug()))
                        .append("</loc></url>"));
        xml.append("</urlset>");
        return ResponseEntity.ok().contentType(new MediaType("application", "xml", StandardCharsets.UTF_8)).body(xml.toString());
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public record VisitBody(boolean consent, String path, String device) {
    }

    public record ContactBody(String name, String email, String message, String companyWebsite) {
    }
}
