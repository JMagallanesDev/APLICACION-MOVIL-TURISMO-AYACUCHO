package com.huamanga.tourism.negocio;

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
 * Directorio de negocios: registro PENDIENTE (RF-104), invisible al público
 * hasta la aprobación (RF-105), panel propio (RF-107) y WhatsApp normalizado
 * (RF-110).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NegocioApiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4")
                    .asCompatibleSubstituteFor("postgres"));

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    // RESTAURANTES (seed V5)
    private static final String CAT_RESTAURANTES = "01980000-0000-7000-8000-000000000501";

    @Autowired
    private TestRestTemplate rest;

    private static String negocioId;

    @Test
    @Order(1)
    @DisplayName("Categorías públicas: 7 traducidas; registro sin login: 401")
    void categoriasYAuth() {
        ResponseEntity<List> cats = rest.getForEntity("/categorias-negocio?idioma=en", List.class);
        assertThat(cats.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cats.getBody()).hasSize(7);
        assertThat((List<Map<String, Object>>) cats.getBody())
                .extracting(c -> c.get("nombre")).contains("Restaurants");

        assertThat(rest.postForEntity("/negocios", json(cuerpo("La Casona"), null), Map.class)
                .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(2)
    @DisplayName("Registro: 201 PENDIENTE con WhatsApp normalizado; duplicado: 409 (RF-104)")
    void registroPendiente() {
        registrar("dueno@test.pe", "Clave1234");
        String token = login("dueno@test.pe", "Clave1234");

        ResponseEntity<Map> res = rest.postForEntity("/negocios",
                json(cuerpo("La Casona del Puca"), token), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getBody().get("estado")).isEqualTo("PENDIENTE");
        assertThat(res.getBody().get("whatsapp")).isEqualTo("51966123456"); // el '+' se normaliza
        negocioId = (String) res.getBody().get("id");

        assertThat(rest.postForEntity("/negocios", json(cuerpo("Otro"), token), Map.class)
                .getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @Order(3)
    @DisplayName("El público no ve PENDIENTES; tras aprobar aparece (RF-105)")
    void aprobacionYListadoPublico() {
        ResponseEntity<Map> antes = rest.getForEntity("/negocios?idioma=es", Map.class);
        assertThat(antes.getBody().get("totalElements")).isEqualTo(0);

        String admin = login("admin@turismohuamanga.pe", "CambiarEnProd_2026!");
        ResponseEntity<Void> aprobado = rest.exchange("/admin/moderacion/negocios/" + negocioId,
                HttpMethod.PATCH, json(Map.of("estado", "APROBADO"), admin), Void.class);
        assertThat(aprobado.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Map> despues = rest.getForEntity("/negocios?idioma=es", Map.class);
        assertThat(despues.getBody().get("totalElements")).isEqualTo(1);
        List<Map<String, Object>> contenido = (List<Map<String, Object>>) despues.getBody().get("content");
        assertThat(contenido.get(0).get("nombre")).isEqualTo("La Casona del Puca");
        assertThat(contenido.get(0).get("descripcion")).isEqualTo("Comida ayacuchana tradicional.");
    }

    @Test
    @Order(4)
    @DisplayName("Panel propio: ver estado y editar mi negocio (RF-107)")
    void panelPropio() {
        String token = login("dueno@test.pe", "Clave1234");

        ResponseEntity<Map> mio = conToken("/negocios/mio", token);
        assertThat(mio.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mio.getBody().get("estado")).isEqualTo("APROBADO");

        Map<String, Object> edicion = cuerpo("La Casona del Puca Renovada");
        ResponseEntity<Map> editado = rest.exchange("/negocios/mio", HttpMethod.PUT,
                json(edicion, token), Map.class);
        assertThat(editado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(editado.getBody().get("nombre")).isEqualTo("La Casona del Puca Renovada");
    }

    // ------------------------------------------------------------------

    private Map<String, Object> cuerpo(String nombre) {
        Map<String, Object> m = new java.util.HashMap<>();
        m.put("categoriaNegocioId", CAT_RESTAURANTES);
        m.put("nombre", nombre);
        m.put("whatsapp", "+51966123456");
        m.put("direccion", "Jr. 28 de Julio 123");
        m.put("horario", "Lun–Dom 12:00–22:00");
        m.put("descripcionEs", "Comida ayacuchana tradicional.");
        m.put("descripcionEn", "Traditional Ayacucho cuisine.");
        return m;
    }

    private HttpEntity<Object> json(Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    private ResponseEntity<Map> conToken(String ruta, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return rest.exchange(ruta, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
    }

    private void registrar(String email, String password) {
        rest.postForEntity("/auth/register",
                json(Map.of("email", email, "password", password, "nombre", "Dueño Test"), null), Map.class);
    }

    private String login(String email, String password) {
        ResponseEntity<Map> res = rest.postForEntity("/auth/login",
                json(Map.of("email", email, "password", password), null), Map.class);
        return (String) res.getBody().get("accessToken");
    }
}
