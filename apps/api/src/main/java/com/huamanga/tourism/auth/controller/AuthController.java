package com.huamanga.tourism.auth.controller;

import com.huamanga.tourism.auth.dto.LoginRequest;
import com.huamanga.tourism.auth.dto.RegisterRequest;
import com.huamanga.tourism.auth.dto.TokenResponse;
import com.huamanga.tourism.auth.dto.UsuarioResponse;
import com.huamanga.tourism.auth.service.AuthService;
import com.huamanga.tourism.common.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Endpoints /api/v1/auth (RF-31, RF-32). El refresh token viaja SOLO como
 * cookie httpOnly + Secure + SameSite=Lax limitada a /api/v1/auth: ni XSS
 * puede leerla ni se envía al resto del API.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String COOKIE_REFRESH = "refresh_token";

    private final AuthService authService;
    private final boolean cookieSecure;

    public AuthController(AuthService authService,
                          @Value("${seguridad.cookie-secure}") boolean cookieSecure) {
        this.authService = authService;
        this.cookieSecure = cookieSecure;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthService.ResultadoAuth resultado = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookieRefresh(resultado.refreshToken()).toString())
                .body(resultado.tokens());
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.ResultadoAuth resultado = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieRefresh(resultado.refreshToken()).toString())
                .body(resultado.tokens());
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(name = COOKIE_REFRESH, required = false) String refreshToken) {
        AuthService.ResultadoAuth resultado = authService.refrescar(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieRefresh(resultado.refreshToken()).toString())
                .body(resultado.tokens());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = COOKIE_REFRESH, required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieBorrado().toString())
                .build();
    }

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return authService.me(autenticado);
    }

    // ------------------------------------------------------------------

    private ResponseCookie cookieRefresh(String valor) {
        return ResponseCookie.from(COOKIE_REFRESH, valor)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ofMillis(authService.getRefreshExpiracionMs()))
                .build();
    }

    private ResponseCookie cookieBorrado() {
        return ResponseCookie.from(COOKIE_REFRESH, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();
    }
}
