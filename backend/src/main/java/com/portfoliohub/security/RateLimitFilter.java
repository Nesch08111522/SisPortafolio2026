package com.portfoliohub.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfoliohub.common.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ClientIp clientIp;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Deque<Long>> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(ClientIp clientIp, ObjectMapper objectMapper) {
        this.clientIp = clientIp;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Limit limit = limitFor(request);
        if (limit != null) {
            String key = limit.name() + ":" + clientIp.resolve(request);
            long now = System.currentTimeMillis();
            Deque<Long> hits = windows.computeIfAbsent(key, ignored -> new ArrayDeque<>());
            synchronized (hits) {
                while (!hits.isEmpty() && now - hits.peekFirst() > limit.windowMs()) {
                    hits.removeFirst();
                }
                if (hits.size() >= limit.max()) {
                    response.setStatus(429);
                    response.setContentType("application/json");
                    objectMapper.writeValue(response.getWriter(), new ApiError(
                            Instant.now(),
                            429,
                            "RATE_LIMIT",
                            "Demasiados intentos. Espera un momento y vuelve a intentarlo.",
                            null
                    ));
                    return;
                }
                hits.addLast(now);
            }
        }
        filterChain.doFilter(request, response);
    }

    private Limit limitFor(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return null;
        }
        String path = request.getRequestURI();
        if (path.endsWith("/api/v1/auth/login")) {
            return new Limit("login", 10, 10 * 60_000L);
        }
        if (path.endsWith("/api/v1/auth/register")) {
            return new Limit("register", 5, 60 * 60_000L);
        }
        if (path.endsWith("/api/v1/auth/forgot-password")) {
            return new Limit("forgot", 5, 60 * 60_000L);
        }
        if (path.contains("/api/v1/public/") && path.endsWith("/contact")) {
            return new Limit("contact", 8, 60 * 60_000L);
        }
        return null;
    }

    private record Limit(String name, int max, long windowMs) {
    }
}
