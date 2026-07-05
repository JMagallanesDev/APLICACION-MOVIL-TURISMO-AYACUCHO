package com.huamanga.tourism.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flujo completo de autenticación contra PostGIS + Redis reales:
 * registro, login, me, roles (401/403, RNF-16) y rotación del refresh token.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthFlowTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4")
                    .asCompatibleSubstituteFor("postgres"));

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Autowired
    private TestRestTemplate rest;

    private static final String EMAIL = "turista@test.pe";
    private static final String PASSWORD = "Segura123";

    @Test
    @Order(1)
    @DisplayName("Registro: 201, access token en body y refresh en cookie httpOnly")
    void registroExitoso() {
        ResponseEntity<Map> res = post("/auth/register",
                Map.of("email", EMAIL, "password", PASSWORD, "nombre", "Turista Test"));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getBody()).containsKey("accessToken");
        assertThat(((Map<?, ?>) res.getBody().get("usuario")).get("rol")).isEqualTo("USUARIO");

        String cookie = res.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(cookie).contains("refresh_token=").contains("HttpOnly").contains("SameSite=Lax");
    }

    @Test
    @Order(2)
    @DisplayName("Registro duplicado: 409 con mensaje claro")
    void registroDuplicado() {
        ResponseEntity<Map> res = post("/auth/register",
                Map.of("email", EMAIL, "password", PASSWORD, "nombre", "Otro"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(res.getBody().get("error")).isEqualTo("EMAIL_YA_REGISTRADO");
    }

    @Test
    @Order(3)
    @DisplayName("Login con contraseña errada: 401 sin revelar cuál campo falló")
    void loginInvalido() {
        ResponseEntity<Map> res = post("/auth/login",
                Map.of("email", EMAIL, "password", "Incorrecta9"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(res.getBody().get("error")).isEqualTo("CREDENCIALES_INVALIDAS");
    }

    @Test
    @Order(4)
    @DisplayName("/auth/me sin token: 401 (RNF-16)")
    void meSinToken() {
        ResponseEntity<Map> res = rest.getForEntity("/auth/me", Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(5)
    @DisplayName("/auth/me con token inválido: 401")
    void meTokenInvalido() {
        ResponseEntity<Map> res = getConToken("/auth/me", "token-falso-123");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(6)
    @DisplayName("Login + /auth/me: 200 con los datos del usuario")
    void loginYMe() {
        String accessToken = loginYObtenerToken(EMAIL, PASSWORD);
        ResponseEntity<Map> me = getConToken("/auth/me", accessToken);
        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(me.getBody().get("email")).isEqualTo(EMAIL);
    }

    @Test
    @Order(7)
    @DisplayName("Rol USUARIO en endpoint ADMIN: 403; rol ADMIN: 200 (RNF-16)")
    void autorizacionPorRol() {
        String tokenUsuario = loginYObtenerToken(EMAIL, PASSWORD);
        assertThat(getConToken("/admin/ping", tokenUsuario).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        String tokenAdmin = loginYObtenerToken("admin@turismohuamanga.pe", "CambiarEnProd_2026!");
        assertThat(getConToken("/admin/ping", tokenAdmin).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(8)
    @DisplayName("Rotación del refresh: el nuevo funciona y el usado queda invalidado")
    void rotacionDeRefreshToken() {
        ResponseEntity<Map> login = post("/auth/login", Map.of("email", EMAIL, "password", PASSWORD));
        String cookieOriginal = extraerCookieRefresh(login);

        ResponseEntity<Map> refresh1 = postConCookie("/auth/refresh", cookieOriginal);
        assertThat(refresh1.getStatusCode()).isEqualTo(HttpStatus.OK);
        String cookieNueva = extraerCookieRefresh(refresh1);
        assertThat(cookieNueva).isNotEqualTo(cookieOriginal);

        // Reusar el refresh ya rotado debe fallar (rotación efectiva)
        ResponseEntity<Map> reuso = postConCookie("/auth/refresh", cookieOriginal);
        assertThat(reuso.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(9)
    @DisplayName("Logout: 204 y la cookie queda invalidada en el servidor")
    void logout() {
        ResponseEntity<Map> login = post("/auth/login", Map.of("email", EMAIL, "password", PASSWORD));
        String cookie = extraerCookieRefresh(login);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookie);
        ResponseEntity<Void> logout = rest.exchange("/auth/logout", HttpMethod.POST,
                new HttpEntity<>(null, headers), Void.class);
        assertThat(logout.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(postConCookie("/auth/refresh", cookie).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ------------------------------------------------------------------

    private ResponseEntity<Map> post(String ruta, Map<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.postForEntity(ruta, new HttpEntity<>(body, headers), Map.class);
    }

    private ResponseEntity<Map> postConCookie(String ruta, String cookie) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookie);
        return rest.exchange(ruta, HttpMethod.POST, new HttpEntity<>(null, headers), Map.class);
    }

    private ResponseEntity<Map> getConToken(String ruta, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return rest.exchange(ruta, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
    }

    private String loginYObtenerToken(String email, String password) {
        ResponseEntity<Map> res = post("/auth/login", Map.of("email", email, "password", password));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) res.getBody().get("accessToken");
    }

    /** Devuelve "refresh_token=<valor>" del Set-Cookie. */
    private String extraerCookieRefresh(ResponseEntity<?> res) {
        String setCookie = res.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).isNotNull();
        return setCookie.split(";")[0];
    }
}
