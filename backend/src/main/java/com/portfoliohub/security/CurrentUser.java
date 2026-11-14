package com.portfoliohub.security;

import com.portfoliohub.common.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static UserPrincipal require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw ApiException.unauthorized("Inicia sesión para continuar.");
        }
        return principal;
    }

    public static java.util.UUID id() {
        return require().id();
    }

    public static boolean hasRole(String role) {
        return require().authorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
