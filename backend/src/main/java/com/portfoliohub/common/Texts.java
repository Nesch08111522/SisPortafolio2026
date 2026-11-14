package com.portfoliohub.common;

import com.portfoliohub.domain.AcademicStatus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;

public final class Texts {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Set<String> RESERVED_SLUGS = Set.of(
            "admin", "api", "app", "auth", "p", "public", "assets", "sitemap", "robots",
            "login", "registro", "ejemplos", "terminos", "portfoliohub"
    );

    private Texts() {
    }

    public static String token() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static String sha256(byte[] value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static void checkPassword(String password) {
        if (password == null || password.length() < 10 || password.length() > 72
                || password.chars().noneMatch(Character::isUpperCase)
                || password.chars().noneMatch(Character::isLowerCase)
                || password.chars().noneMatch(Character::isDigit)
                || password.chars().allMatch(Character::isLetterOrDigit)) {
            throw ApiException.badRequest(
                    "PASSWORD_WEAK",
                    "La contraseña debe tener entre 10 y 72 caracteres, con mayúscula, minúscula, número y símbolo."
            );
        }
    }

    public static String slug(String input) {
        String normalized = Normalizer.normalize(input == null ? "" : input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (normalized.length() > 60) {
            normalized = normalized.substring(0, 60).replaceAll("-$", "");
        }
        return normalized;
    }

    public static void checkSlug(String slug) {
        if (slug == null || !slug.matches("[a-z0-9]+(?:-[a-z0-9]+)*") || slug.length() < 3 || slug.length() > 60) {
            throw ApiException.badRequest("INVALID_SLUG", "El identificador debe tener entre 3 y 60 caracteres: letras, números y guiones.");
        }
        if (RESERVED_SLUGS.contains(slug)) {
            throw ApiException.badRequest("RESERVED_SLUG", "Ese identificador está reservado.");
        }
    }

    public static String cleanLink(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > 255) {
            throw ApiException.badRequest("INVALID_URL", "El enlace es demasiado largo.");
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("vbscript:")) {
            throw ApiException.badRequest("INVALID_URL", "Ese enlace no está permitido.");
        }
        if (trimmed.contains("://") && !(lower.startsWith("https://") || lower.startsWith("http://"))) {
            throw ApiException.badRequest("INVALID_URL", "Solo se permiten enlaces http o https.");
        }
        return trimmed;
    }

    public static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String limit(String value, int max) {
        String trimmed = blankToNull(value);
        if (trimmed != null && trimmed.length() > max) {
            throw ApiException.badRequest("TEXT_TOO_LONG", "Uno de los textos supera los " + max + " caracteres.");
        }
        return trimmed;
    }

    public static String academicLabel(AcademicStatus status, String careerName, boolean studentSafe, boolean licenseDeclared) {
        String career = careerName == null || careerName.isBlank() ? "su formación" : careerName;
        String prefix = switch (status) {
            case ESTUDIANTE -> "Estudiante de ";
            case EGRESADO -> "Egresado de ";
            case BACHILLER -> "Bachiller de ";
            case TITULADO -> "Titulado en ";
            case PROFESIONAL -> "Profesional de ";
            case POSGRADO -> "Estudiante de posgrado en ";
        };
        if (studentSafe && (status == AcademicStatus.TITULADO || status == AcademicStatus.PROFESIONAL) && licenseDeclared) {
            return prefix + career;
        }
        return prefix + career;
    }
}
