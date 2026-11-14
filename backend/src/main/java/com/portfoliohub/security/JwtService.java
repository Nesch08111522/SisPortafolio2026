package com.portfoliohub.security;

import com.portfoliohub.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final AppProperties properties;

    public JwtService(AppProperties properties) {
        this.properties = properties;
    }

    public String createAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.getJwt().getAccessMinutes() * 60L);
        List<String> roles = principal.authorities().stream().map(authority -> authority.getAuthority()).toList();
        return Jwts.builder()
                .subject(principal.id().toString())
                .claim("email", principal.email())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }

    public long accessSeconds() {
        return properties.getJwt().getAccessMinutes() * 60L;
    }

    private SecretKey key() {
        String secret = properties.getJwt().getSecret();
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("El secreto JWT debe tener al menos 32 caracteres.");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
