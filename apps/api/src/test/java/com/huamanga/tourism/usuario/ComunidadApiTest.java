package com.huamanga.tourism.usuario;

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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cierre del módulo 5: favoritos (RF-35, marcar idempotente + listar + quitar)
 * y reporte de contenido (RF-45, duplicado 409 + 3 reportes = EN_REVISION y
 * fuera del listado público).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ComunidadApiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4")
                    .asCompatibleSubstituteFor("postgres"));

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    private static final String SLUG = "plaza-mayor-de-ayacucho";

    @Autowired
    private TestRestTemplate rest;

    private static String resenaId;

    @Test
    @Order(1)
    @DisplayName("Favoritos: 401 anónimo; marcar 2 veces es idempotente; listar y quitar (RF-35)")
    void favoritos() {
        assertThat(rest.exchange("/lugares/" + SLUG + "/favorito", HttpMethod.PUT,
                json(null, null), Void.class).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        registrar("fan@test.pe", "Clave1234");
        String token = login("fan@test.pe", "Clave1234");

        // marcar dos veces: idempotente
        assertThat(marcar(token).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(marcar(token).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<List> lista = conToken("/favoritos?idioma=es", token);
        assertThat(lista.getBody()).hasSize(1);
        assertThat(((Map<String, Object>) lista.getBody().get(0)).get("slug")).isEqualTo(SLUG);

        ResponseEntity<List> slugs = conToken("/favoritos/slugs", token);
        assertThat(slugs.getBody()).containsExactly(SLUG);

        assertThat(rest.exchange("/lugares/" + SLUG + "/favorito", HttpMethod.DELETE,
                json(null, token), Void.class).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(conToken("/favoritos?idioma=es", token).getBody()).isEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("Reporte de contenido: duplicado 409; al 3er reporte la reseña pasa a revisión (RF-45)")
    void reporteDeContenido() {
        // Autor publica una reseña
        registrar("autor@test.pe", "Clave1234");
        String autor = login("autor@test.pe", "Clave1234");
        ResponseEntity<Map> resena = rest.postForEntity("/lugares/" + SLUG + "/resenas",
                json(Map.of("calificacion", 1, "comentario", "Contenido cuestionable."), autor), Map.class);
        resenaId = (String) resena.getBody().get("id");

        // Tres usuarios distintos la reportan; el duplicado del primero da 409
        for (int i = 1; i <= 3; i++) {
            registrar("reporta" + i + "@test.pe", "Clave1234");
            String token = login("reporta" + i + "@test.pe", "Clave1234");
            ResponseEntity<Void> res = rest.postForEntity("/reportes-contenido",
                    json(Map.of("resenaId", resenaId, "motivo", "Ofensivo"), token), Void.class);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            if (i == 1) {
                ResponseEntity<Map> duplicado = rest.postForEntity("/reportes-contenido",
                        json(Map.of("resenaId", resenaId, "motivo", "Otra vez"), token), Map.class);
                assertThat(duplicado.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            }
        }

        // La reseña salió del listado público (EN_REVISION)
        ResponseEntity<Map> publicas = rest.getForEntity("/lugares/" + SLUG + "/resenas", Map.class);
        List<Map<String, Object>> contenido = (List<Map<String, Object>>) publicas.getBody().get("content");
        assertThat(contenido).noneMatch(r -> resenaId.equals(r.get("id")));
    }

    // ------------------------------------------------------------------

    private ResponseEntity<Void> marcar(String token) {
        return rest.exchange("/lugares/" + SLUG + "/favorito", HttpMethod.PUT,
                json(null, token), Void.class);
    }

    private ResponseEntity<List> conToken(String ruta, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return rest.exchange(ruta, HttpMethod.GET, new HttpEntity<>(headers), List.class);
    }

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
