package com.portfoliohub.service;

import com.portfoliohub.common.ApiException;
import com.portfoliohub.common.Texts;
import com.portfoliohub.config.AppProperties;
import com.portfoliohub.domain.PersonalProfile;
import com.portfoliohub.domain.ProfessionalProfile;
import com.portfoliohub.domain.RefreshToken;
import com.portfoliohub.domain.Role;
import com.portfoliohub.domain.TokenType;
import com.portfoliohub.domain.User;
import com.portfoliohub.domain.VerificationToken;
import com.portfoliohub.repo.CountryRepository;
import com.portfoliohub.repo.PersonalProfileRepository;
import com.portfoliohub.repo.ProfessionalProfileRepository;
import com.portfoliohub.repo.RefreshTokenRepository;
import com.portfoliohub.repo.RoleRepository;
import com.portfoliohub.repo.UserRepository;
import com.portfoliohub.repo.VerificationTokenRepository;
import com.portfoliohub.security.JwtService;
import com.portfoliohub.security.UserPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AuthService {

    public static final String TERMS_VERSION = "2026-11";
    private static final String GENERIC_LOGIN = "Credenciales inválidas o cuenta temporalmente bloqueada.";

    private final UserRepository users;
    private final RoleRepository roles;
    private final PersonalProfileRepository personalProfiles;
    private final ProfessionalProfileRepository professionalProfiles;
    private final VerificationTokenRepository verificationTokens;
    private final RefreshTokenRepository refreshTokens;
    private final CountryRepository countries;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;
    private final AuditService auditService;
    private final PortfolioService portfolios;
    private final AppProperties properties;
    private final String dummyHash;

    public AuthService(
            UserRepository users,
            RoleRepository roles,
            PersonalProfileRepository personalProfiles,
            ProfessionalProfileRepository professionalProfiles,
            VerificationTokenRepository verificationTokens,
            RefreshTokenRepository refreshTokens,
            CountryRepository countries,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            MailService mailService,
            AuditService auditService,
            PortfolioService portfolios,
            AppProperties properties
    ) {
        this.users = users;
        this.roles = roles;
        this.personalProfiles = personalProfiles;
        this.professionalProfiles = professionalProfiles;
        this.verificationTokens = verificationTokens;
        this.refreshTokens = refreshTokens;
        this.countries = countries;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailService = mailService;
        this.auditService = auditService;
        this.portfolios = portfolios;
        this.properties = properties;
        this.dummyHash = passwordEncoder.encode("dummy-password-not-used");
    }

    @Transactional
    public Map<String, Object> register(RegisterCommand command, String ip) {
        String email = normalizeEmail(command.email());
        if (!command.password().equals(command.confirmPassword())) {
            throw ApiException.badRequest("PASSWORD_MISMATCH", "La confirmación de la contraseña no coincide.");
        }
        Texts.checkPassword(command.password());
        if (!command.acceptTerms()) {
            throw ApiException.badRequest("TERMS_REQUIRED", "Debes aceptar los términos y condiciones.");
        }
        String country = command.countryCode() == null ? "" : command.countryCode().trim().toUpperCase(Locale.ROOT);
        if (!country.isBlank() && (!country.matches("[A-Z]{2}") || countries.findByIso2IgnoreCase(country).isEmpty())) {
            throw ApiException.badRequest("INVALID_COUNTRY", "Selecciona un país de la lista.");
        }
        if (users.findByEmailIgnoreCase(email).isPresent()) {
            throw ApiException.conflict("EMAIL_IN_USE", "Ya existe una cuenta con ese correo.");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(command.password()));
        user.setFirstName(requiredName(command.firstName(), "El nombre es obligatorio."));
        user.setLastName(requiredName(command.lastName(), "Los apellidos son obligatorios."));
        user.setCountryCode(country.isBlank() ? null : country);
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setTermsAcceptedAt(Instant.now());
        user.setTermsVersion(TERMS_VERSION);
        Role userRole = roles.findByName("USER").orElseThrow(() -> new IllegalStateException("Falta el rol USER."));
        user.getRoles().add(userRole);
        users.save(user);

        PersonalProfile personal = new PersonalProfile();
        personal.setUserId(user.getId());
        personalProfiles.save(personal);
        ProfessionalProfile professional = new ProfessionalProfile();
        professional.setUserId(user.getId());
        professionalProfiles.save(professional);

        portfolios.openSite(user.getId());
        auditService.record(user.getId(), "REGISTER", "USER", user.getId().toString(), null, ip);
        return tokens(user);
    }

    @Transactional
    public Map<String, Object> verifyEmail(String token) {
        VerificationToken stored = validToken(token, TokenType.EMAIL_VERIFICATION);
        User user = users.findById(stored.getUserId()).orElseThrow(() -> ApiException.badRequest("INVALID_TOKEN", "El enlace no es válido."));
        user.setEmailVerified(true);
        stored.setUsedAt(Instant.now());
        auditService.record(user.getId(), "VERIFY_EMAIL", "USER", user.getId().toString(), null, null);
        return Map.of("message", "Correo confirmado. Ya puedes iniciar sesión.");
    }

    @Transactional
    public Map<String, Object> resend(String email) {
        return Map.of("message", "La cuenta queda activa al crearla. No enviamos un código por correo.");
    }

    @Transactional
    public Map<String, Object> login(String email, String password, String ip) {
        User user = users.findByEmailIgnoreCase(normalizeEmail(email)).orElse(null);
        if (user == null) {
            passwordEncoder.matches(password, dummyHash);
            throw ApiException.unauthorized(GENERIC_LOGIN);
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw ApiException.unauthorized(GENERIC_LOGIN);
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setLockedUntil(Instant.now().plus(15, ChronoUnit.MINUTES));
                user.setFailedLoginAttempts(0);
            }
            auditService.record(user.getId(), "LOGIN_FAILED", "USER", user.getId().toString(), null, ip);
            throw ApiException.unauthorized(GENERIC_LOGIN);
        }
        if (!user.isEnabled()) {
            throw ApiException.unauthorized("Esta cuenta está deshabilitada.");
        }
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
        }
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        auditService.record(user.getId(), "LOGIN", "USER", user.getId().toString(), null, ip);
        return tokens(user);
    }

    @Transactional
    public Map<String, Object> refresh(String refreshToken) {
        RefreshToken stored = refreshTokens.findByTokenHash(Texts.sha256(refreshToken == null ? "" : refreshToken))
                .filter(token -> !token.isRevoked() && token.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> ApiException.unauthorized("La sesión expiró. Vuelve a iniciar sesión."));
        User user = users.findById(stored.getUserId())
                .filter(found -> found.isEnabled() && found.isEmailVerified())
                .orElseThrow(() -> ApiException.unauthorized("La sesión expiró. Vuelve a iniciar sesión."));
        stored.setRevoked(true);
        return tokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokens.findByTokenHash(Texts.sha256(refreshToken)).ifPresent(token -> token.setRevoked(true));
    }

    @Transactional
    public Map<String, Object> forgot(String email) {
        users.findByEmailIgnoreCase(normalizeEmail(email)).ifPresent(user -> {
            String rawToken = issueToken(user.getId(), TokenType.PASSWORD_RESET, 0);
            VerificationToken token = verificationTokens.findByTokenHashAndType(Texts.sha256(rawToken), TokenType.PASSWORD_RESET).orElseThrow();
            token.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
            String link = properties.getPublicBaseUrl() + "/restablecer?token=" + rawToken;
            mailService.send(user.getEmail(), "Restablece tu contraseña",
                    "Usa este enlace en los próximos 30 minutos:\n" + link + "\n\nSi no lo solicitaste, ignora este mensaje.");
        });
        return Map.of("message", "Si el correo está registrado, enviaremos instrucciones para restablecer la contraseña.");
    }

    @Transactional
    public Map<String, Object> reset(String token, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw ApiException.badRequest("PASSWORD_MISMATCH", "La confirmación de la contraseña no coincide.");
        }
        Texts.checkPassword(password);
        VerificationToken stored = validToken(token, TokenType.PASSWORD_RESET);
        User user = users.findById(stored.getUserId()).orElseThrow(() -> ApiException.badRequest("INVALID_TOKEN", "El enlace no es válido."));
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        stored.setUsedAt(Instant.now());
        refreshTokens.deleteByUserId(user.getId());
        auditService.record(user.getId(), "PASSWORD_RESET", "USER", user.getId().toString(), null, null);
        return Map.of("message", "Contraseña actualizada. Inicia sesión con la nueva contraseña.");
    }

    @Transactional
    public Map<String, Object> changePassword(java.util.UUID userId, String currentPassword, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw ApiException.badRequest("PASSWORD_MISMATCH", "La confirmación de la contraseña no coincide.");
        }
        Texts.checkPassword(password);
        User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("No encontramos tu cuenta."));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw ApiException.badRequest("INVALID_PASSWORD", "La contraseña actual no es correcta.");
        }
        user.setPasswordHash(passwordEncoder.encode(password));
        refreshTokens.deleteByUserId(user.getId());
        auditService.record(userId, "PASSWORD_CHANGE", "USER", userId.toString(), null, null);
        return Map.of("message", "Contraseña actualizada. Vuelve a iniciar sesión.");
    }

    public Map<String, Object> me(User user) {
        return userView(user);
    }

    private Map<String, Object> tokens(User user) {
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList();
        UserPrincipal principal = new UserPrincipal(user.getId(), user.getEmail(), authorities);
        String refresh = Texts.token();
        RefreshToken stored = new RefreshToken();
        stored.setUserId(user.getId());
        stored.setTokenHash(Texts.sha256(refresh));
        stored.setExpiresAt(Instant.now().plus(properties.getJwt().getRefreshDays(), ChronoUnit.DAYS));
        refreshTokens.save(stored);
        return Map.of(
                "accessToken", jwtService.createAccessToken(principal),
                "refreshToken", refresh,
                "tokenType", "Bearer",
                "expiresIn", jwtService.accessSeconds(),
                "user", userView(user)
        );
    }

    private Map<String, Object> userView(User user) {
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "emailVerified", user.isEmailVerified(),
                "onboardingCompleted", user.isOnboardingCompleted(),
                "countryCode", user.getCountryCode() == null ? "" : user.getCountryCode(),
                "roles", user.getRoles().stream().map(Role::getName).sorted().toList()
        );
    }

    private String issueToken(java.util.UUID userId, TokenType type, int hours) {
        verificationTokens.deleteByUserIdAndType(userId, type);
        String raw = Texts.token();
        VerificationToken token = new VerificationToken();
        token.setUserId(userId);
        token.setTokenHash(Texts.sha256(raw));
        token.setType(type);
        token.setExpiresAt(Instant.now().plus(hours <= 0 ? 1 : hours, ChronoUnit.HOURS));
        verificationTokens.save(token);
        return raw;
    }

    private VerificationToken validToken(String raw, TokenType type) {
        if (raw == null || raw.isBlank()) {
            throw ApiException.badRequest("INVALID_TOKEN", "El enlace no es válido.");
        }
        VerificationToken token = verificationTokens.findByTokenHashAndType(Texts.sha256(raw), type)
                .orElseThrow(() -> ApiException.badRequest("INVALID_TOKEN", "El enlace no es válido o ya fue usado."));
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.badRequest("INVALID_TOKEN", "El enlace no es válido o ya fue usado.");
        }
        return token;
    }

    private String normalizeEmail(String email) {
        if (email == null || !email.trim().matches("(?i)^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw ApiException.badRequest("INVALID_EMAIL", "Escribe un correo electrónico válido.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String requiredName(String value, String message) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.length() < 2 || trimmed.length() > 80) {
            throw ApiException.badRequest("INVALID_NAME", message);
        }
        return trimmed;
    }

    public record RegisterCommand(
            String firstName,
            String lastName,
            String email,
            String password,
            String confirmPassword,
            String countryCode,
            boolean acceptTerms
    ) {
    }
}
