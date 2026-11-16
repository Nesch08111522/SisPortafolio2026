package com.portfoliohub.web;

import com.portfoliohub.security.ClientIp;
import com.portfoliohub.security.CurrentUser;
import com.portfoliohub.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;
    private final ClientIp clientIp;
    private final com.portfoliohub.repo.UserRepository users;

    public AuthController(AuthService authService, ClientIp clientIp, com.portfoliohub.repo.UserRepository users) {
        this.authService = authService;
        this.clientIp = clientIp;
        this.users = users;
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
        return authService.register(new AuthService.RegisterCommand(
                request.firstName(), request.lastName(), request.email(), request.password(),
                request.confirmPassword(), request.countryCode(), request.acceptTerms()
        ), clientIp.resolve(http));
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return authService.login(request.email(), request.password(), clientIp.resolve(http));
    }

    @PostMapping("/auth/refresh")
    public Map<String, Object> refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody(required = false) RefreshRequest request) {
        authService.logout(request == null ? null : request.refreshToken());
    }

    @PostMapping("/auth/verify-email")
    public Map<String, Object> verify(@Valid @RequestBody TokenRequest request) {
        return authService.verifyEmail(request.token());
    }

    @PostMapping("/auth/resend-verification")
    public Map<String, Object> resend(@Valid @RequestBody EmailRequest request) {
        return authService.resend(request.email());
    }

    @PostMapping("/auth/forgot-password")
    public Map<String, Object> forgot(@Valid @RequestBody EmailRequest request) {
        return authService.forgot(request.email());
    }

    @PostMapping("/auth/reset-password")
    public Map<String, Object> reset(@Valid @RequestBody ResetRequest request) {
        return authService.reset(request.token(), request.password(), request.confirmPassword());
    }

    @GetMapping("/users/me")
    public Map<String, Object> me() {
        return authService.me(users.findById(CurrentUser.id()).orElseThrow());
    }

    @PatchMapping("/users/me/password")
    public Map<String, Object> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        return authService.changePassword(CurrentUser.id(), request.currentPassword(), request.password(), request.confirmPassword());
    }

    public record RegisterRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @Email @NotBlank String email,
            @NotBlank String password,
            @NotBlank String confirmPassword,
            String countryCode,
            boolean acceptTerms
    ) {
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record RefreshRequest(String refreshToken) {
    }

    public record TokenRequest(@NotBlank String token) {
    }

    public record EmailRequest(@Email @NotBlank String email) {
    }

    public record ResetRequest(@NotBlank String token, @NotBlank String password, @NotBlank String confirmPassword) {
    }

    public record PasswordChangeRequest(@NotBlank String currentPassword, @NotBlank String password, @NotBlank String confirmPassword) {
    }
}
