package com.huamanga.tourism.resena;

import com.huamanga.tourism.lugar.service.EstadisticaLugarService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reseñas (RF-37) + promedio vía vista materializada (6.3) + moderación
 * (RF-50) + fotos sin Cloudinary configurado (degradación, RF-38).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResenaApiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4")
                    .asCompatibleSubstituteFor("postgres"));

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    private static final String SLUG = "catedral-de-ayacucho";

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private EstadisticaLugarService estadisticaService;

    private static String resenaId;

    @Test
    @Order(1)
    @DisplayName("Crear reseña: 201; duplicada: 409; calificación 6: 400")
    void crearResena() {
        registrar("viajero@test.pe", "Clave1234");
        String token = login("viajero@test.pe", "Clave1234");

        ResponseEntity<Map> creada = rest.postForEntity("/lugares/" + SLUG + "/resenas",
                json(Map.of("calificacion", 5, "comentario", "Imponente, los retablos brillan."), token),
                Map.class);
        assertThat(creada.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        resenaId = (String) creada.getBody().get("id");

        assertThat(rest.postForEntity("/lugares/" + SLUG + "/resenas",
                json(Map.of("calificacion", 4), token), Map.class).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(rest.postForEntity("/lugares/" + SLUG + "/resenas",
                json(Map.of("calificacion", 6), token), Map.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(2)
    @DisplayName("Listado público con autor; el promedio llega tras el refresh de la vista")
    void promedioViaVistaMaterializada() {
        ResponseEntity<Map> lista = rest.getForEntity("/lugares/" + SLUG + "/resenas", Map.class);
        List<Map<String, Object>> contenido = (List<Map<String, Object>>) lista.getBody().get("content");
        assertThat(contenido).hasSize(1);
        assertThat(contenido.get(0).get("autorNombre")).isEqualTo("Test");

        // El mismo job que corre cada 5 min con ShedLock (REFRESH CONCURRENTLY)
        estadisticaService.refrescar();

        ResponseEntity<Map> detalle = rest.getForEntity("/lugares/" + SLUG, Map.class);
        assertThat(Double.parseDouble(String.valueOf(detalle.getBody().get("calificacionPromedio"))))
                .isEqualTo(5.0);
        assertThat(detalle.getBody().get("totalResenas")).isEqualTo(1);
    }

    @Test
    @Order(3)
    @DisplayName("Moderación: USUARIO 403; ADMIN oculta y desaparece del público (RF-50)")
    void moderacionDeResena() {
        String tokenUsuario = login("viajero@test.pe", "Clave1234");
        ResponseEntity<Void> prohibido = rest.exchange("/admin/moderacion/resenas/" + resenaId,
                HttpMethod.PATCH, json(Map.of("estado", "OCULTA"), tokenUsuario), Void.class);
        assertThat(prohibido.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        String tokenAdmin = login("admin@turismohuamanga.pe", "CambiarEnProd_2026!");
        ResponseEntity<Void> ocultada = rest.exchange("/admin/moderacion/resenas/" + resenaId,
                HttpMethod.PATCH, json(Map.of("estado", "OCULTA"), tokenAdmin), Void.class);
        assertThat(ocultada.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Map> lista = rest.getForEntity("/lugares/" + SLUG + "/resenas", Map.class);
        assertThat(lista.getBody().get("totalElements")).isEqualTo(0);
    }

    @Test
    @Order(4)
    @DisplayName("Fotos sin Cloudinary configurado: 503 con código claro")
    void fotosSinCloudinary() {
        String token = login("viajero@test.pe", "Clave1234");

        MultiValueMap<String, Object> cuerpo = new LinkedMultiValueMap<>();
        cuerpo.add("archivo", new ByteArrayResource(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1}) {
            @Override
            public String getFilename() {
                return "foto.jpg";
            }
        });
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(token);

        ResponseEntity<Map> res = rest.postForEntity("/lugares/" + SLUG + "/fotos",
                new HttpEntity<>(cuerpo, headers), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(res.getBody().get("error")).isEqualTo("FOTOS_NO_DISPONIBLES");

        // La galería pública responde vacía, nunca rota
        assertThat(rest.getForEntity("/lugares/" + SLUG + "/fotos", Map.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    // ------------------------------------------------------------------

    private HttpEntity<Object> json(Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return new HttpEntity<>(body, headers);
    }

    private void registrar(String email, String password) {
        rest.postForEntity("/auth/register",
                json(Map.of("email", email, "password", password, "nombre", "Test"), null), Map.class);
    }

    private String login(String email, String password) {
        ResponseEntity<Map> res = rest.postForEntity("/auth/login",
                json(Map.of("email", email, "password", password), null), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) res.getBody().get("accessToken");
    }
}
