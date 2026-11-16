package com.portfoliohub.config;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class EmbeddedPostgresEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static EmbeddedPostgres postgres;
    private static String jdbcUrl;

    private static EmbeddedPostgres open(Path dataDirectory) throws IOException {
        Files.createDirectories(dataDirectory);
        return EmbeddedPostgres.builder()
                .setPort(0)
                .setDataDirectory(dataDirectory)
                .setCleanDataDirectory(false)
                .setPGStartupWait(Duration.ofSeconds(60))
                .start();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String profiles = System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "");
        String configuredUrl = environment.getProperty("spring.datasource.url");
        if (configuredUrl == null || configuredUrl.isBlank()) {
            configuredUrl = System.getenv("SPRING_DATASOURCE_URL");
        }
        if (profiles.contains("prod") || (configuredUrl != null && !configuredUrl.isBlank())) {
            return;
        }
        synchronized (EmbeddedPostgresEnvironmentPostProcessor.class) {
            if (postgres == null) {
                Exception lastFailure = null;
                for (String folder : new String[] {"pg-live", "pg-app", "pg", "postgres-local", "postgres"}) {
                    try {
                        postgres = open(Path.of("data", folder));
                        lastFailure = null;
                        break;
                    } catch (IOException | IllegalStateException exception) {
                        lastFailure = exception;
                    }
                }
                if (postgres == null) {
                    throw new IllegalStateException(
                            "No se pudo iniciar PostgreSQL embebido. Instala PostgreSQL o define SPRING_DATASOURCE_URL.",
                            lastFailure
                    );
                }
                jdbcUrl = postgres.getJdbcUrl("postgres", "postgres");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        postgres.close();
                    } catch (IOException ignored) {
                        // El proceso ya está terminando.
                    }
                }));
            }
        }
        Map<String, Object> values = new HashMap<>();
        values.put("spring.datasource.url", jdbcUrl);
        values.put("spring.datasource.username", "postgres");
        values.put("spring.datasource.password", "postgres");
        values.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
        environment.getPropertySources().addFirst(new MapPropertySource("embeddedPostgres", values));
    }
}
