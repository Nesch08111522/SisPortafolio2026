package com.portfoliohub.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

public record UserPrincipal(UUID id, String email, List<SimpleGrantedAuthority> authorities) {
}
