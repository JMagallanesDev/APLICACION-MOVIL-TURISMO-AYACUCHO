package com.huamanga.tourism.evento;

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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Agenda cultural: eventos demo del seed V7, filtro por rango ("Durante mi
 * visita", RF-84b), detalle bilingüe y escritura solo ADMIN (RF-86).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class EventoApiTest {

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
    @DisplayName("GET /eventos por rango: Semana Santa 2026 aparece en es y en (RF-84b)")
    void eventosEnRangoBilingue() {
        ResponseEntity<List> es = rest.getForEntity(
                "/eventos?idioma=es&desde=2026-03-01&hasta=2026-04-30", List.class);
        assertThat(es.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> eventos = es.getBody();
        assertThat(eventos).extracting(e -> e.get("nombre"))
                .contains("Semana Santa de Ayacucho");

        ResponseEntity<List> en = rest.getForEntity(
                "/eventos?idioma=en&desde=2026-03-01&hasta=2026-04-30", List.class);
        assertThat((List<Map<String, Object>>) en.getBody())
                .extracting(e -> e.get("nombre")).contains("Ayacucho Holy Week");
    }

    @Test
    @DisplayName("GET /eventos/proximos: publicados ordenados por fecha")
    void proximosEventos() {
        ResponseEntity<List> res = rest.getForEntity("/eventos/proximos?idioma=es&limite=10", List.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isNotEmpty();
    }

    @Test
    @DisplayName("Escritura de eventos exige ADMIN: anónimo 401, USUARIO 403 (RF-86)")
    void escrituraProtegida() {
        Map<String, Object> nuevo = Map.of(
                "tipo", "CONCIERTO",
                "distritoId", "01980000-0000-7000-8000-000000000301",
                "fechaInicio", "2026-09-10", "fechaFin", "2026-09-10",
                "recurrenteAnual", false, "estado", "PUBLICADO",
                "traducciones", List.of(Map.of("idioma", "es", "nombre", "Concierto de prueba")));

        assertThat(rest.postForEntity("/eventos", json(nuevo, null), Map.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        registrar("melomano@test.pe", "Clave1234");
        assertThat(rest.postForEntity("/eventos",
                json(nuevo, login("melomano@test.pe", "Clave1234")), Map.class).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("ADMIN crea, clona a otro año (RF-86) y elimina un evento")
    void cicloAdmin() {
        String admin = login("admin@turismohuamanga.pe", "CambiarEnProd_2026!");
        Map<String, Object> nuevo = Map.of(
                "tipo", "CONCIERTO",
                "distritoId", "01980000-0000-7000-8000-000000000301",
                "fechaInicio", "2026-09-10", "fechaFin", "2026-09-10",
                "recurrenteAnual", true, "estado", "PUBLICADO",
                "traducciones", List.of(
                        Map.of("idioma", "es", "nombre", "Concierto sinfónico"),
                        Map.of("idioma", "en", "nombre", "Symphony concert")));

        ResponseEntity<Map> creado = rest.postForEntity("/eventos", json(nuevo, admin), Map.class);
        assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String id = (String) creado.getBody().get("id");

        // Clonar al año siguiente: nace en BORRADOR con fecha +1 año
        ResponseEntity<Map> clon = rest.postForEntity("/eventos/" + id + "/clonar?anios=1",
                json(null, admin), Map.class);
        assertThat(clon.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(clon.getBody().get("fechaInicio")).isEqualTo("2027-09-10");
        assertThat(clon.getBody().get("estado")).isEqualTo("BORRADOR");

        // Eliminar el original
        ResponseEntity<Void> borrado = rest.exchange("/eventos/" + id, HttpMethod.DELETE,
                json(null, admin), Void.class);
        assertThat(borrado.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(rest.getForEntity("/eventos/" + id, Map.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ------------------------------------------------------------------

    private HttpEntity<Object> json(Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    private void registrar(String email, String password) {
        rest.postForEntity("/auth/register",
                json(Map.of("email", email, "password", password, "nombre", "Test"), null), Map.class);
    }

    private String login(String email, String password) {
        ResponseEntity<Map> res = rest.postForEntity("/auth/login",
                json(Map.of("email", email, "password", password), null), Map.class);
        return (String) res.getBody().get("accessToken");
    }
}
