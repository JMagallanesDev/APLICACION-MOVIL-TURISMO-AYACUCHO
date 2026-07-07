package com.huamanga.tourism.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

/** Dashboard del admin (RF-52): protegido por rol y con agregados del seed. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AdminMetricasTest {

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

    @Test
    @DisplayName("GET /admin/metricas: anónimo 401, USUARIO 403, ADMIN 200 con conteos")
    void metricasProtegidasYConDatos() {
        assertThat(rest.getForEntity("/admin/metricas", Map.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        registrar("user@test.pe", "Clave1234");
        assertThat(conToken("/admin/metricas", login("user@test.pe", "Clave1234")).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<Map> res =
                conToken("/admin/metricas", login("admin@turismohuamanga.pe", "CambiarEnProd_2026!"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        // El seed publica 5 lugares
        assertThat(res.getBody().get("lugaresPublicados")).isEqualTo(5);
        assertThat(res.getBody()).containsKeys("fotosPendientes", "reportesRecibidos", "usuariosRegistrados");
    }

    private ResponseEntity<Map> conToken(String ruta, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return rest.exchange(ruta, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
    }

    private void registrar(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        rest.postForEntity("/auth/register", new HttpEntity<>(
                Map.of("email", email, "password", password, "nombre", "Test"), headers), Map.class);
    }

    private String login(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> res = rest.postForEntity("/auth/login", new HttpEntity<>(
                Map.of("email", email, "password", password), headers), Map.class);
        return (String) res.getBody().get("accessToken");
    }
}
