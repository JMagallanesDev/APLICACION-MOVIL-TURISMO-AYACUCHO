package com.huamanga.tourism.lugar;

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
 * CRUD de lugares (RF-47): lectura pública con el seed, escritura solo ADMIN,
 * validación de bounds de Ayacucho (RF-22b) y soft delete.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LugarApiTest {

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

    private static String idLugarCreado;

    @Test
    @Order(1)
    @DisplayName("GET /lugares: público, devuelve los 5 del seed con nombre en español")
    void listadoPublico() {
        ResponseEntity<Map> res = rest.getForEntity("/lugares", Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().get("totalElements")).isEqualTo(5);

        List<Map<String, Object>> contenido = (List<Map<String, Object>>) res.getBody().get("content");
        assertThat(contenido).extracting(l -> l.get("slug"))
                .contains("catedral-de-ayacucho", "plaza-mayor-de-ayacucho");
    }

    @Test
    @Order(2)
    @DisplayName("GET /lugares?categoria=IGLESIAS: filtra (RF-04)")
    void filtroPorCategoria() {
        ResponseEntity<Map> res = rest.getForEntity("/lugares?categoria=IGLESIAS", Map.class);
        assertThat(res.getBody().get("totalElements")).isEqualTo(2); // Catedral + Santo Domingo
    }

    @Test
    @Order(3)
    @DisplayName("GET /lugares/{slug}: ficha con traducciones es/en y horarios")
    void detallePorSlug() {
        ResponseEntity<Map> res = rest.getForEntity("/lugares/catedral-de-ayacucho", Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) res.getBody().get("traducciones")).hasSize(2);
        assertThat((List<?>) res.getBody().get("horarios")).isNotEmpty();
        assertThat(res.getBody().get("categoriaCodigo")).isEqualTo("IGLESIAS");
    }

    @Test
    @Order(3)
    @DisplayName("Búsqueda full-text ?q= encuentra en es y en (RF-02)")
    void busquedaFullText() {
        // "mirador" aparece en la descripción es del Mirador de Acuchimay
        ResponseEntity<Map> es = rest.getForEntity("/lugares?q=mirador", Map.class);
        assertThat(es.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> contenidoEs = (List<Map<String, Object>>) es.getBody().get("content");
        assertThat(contenidoEs).extracting(l -> l.get("slug")).contains("mirador-de-acuchimay");

        // "cathedral" solo existe en la traducción en inglés
        ResponseEntity<Map> en = rest.getForEntity("/lugares?q=cathedral", Map.class);
        List<Map<String, Object>> contenidoEn = (List<Map<String, Object>>) en.getBody().get("content");
        assertThat(contenidoEn).extracting(l -> l.get("slug")).contains("catedral-de-ayacucho");

        // sin resultados razonables
        ResponseEntity<Map> nada = rest.getForEntity("/lugares?q=xyznoexiste", Map.class);
        assertThat(nada.getBody().get("totalElements")).isEqualTo(0);
    }

    @Test
    @Order(3)
    @DisplayName("abiertoAhora presente (RF-09b): Plaza 24h siempre true")
    void abiertoAhoraEnCards() {
        ResponseEntity<Map> res = rest.getForEntity("/lugares", Map.class);
        List<Map<String, Object>> contenido = (List<Map<String, Object>>) res.getBody().get("content");

        Map<String, Object> plaza = contenido.stream()
                .filter(l -> "plaza-mayor-de-ayacucho".equals(l.get("slug")))
                .findFirst().orElseThrow();
        // La Plaza abre 00:00-23:59 todos los días: abierta a cualquier hora del test
        assertThat(plaza.get("abiertoAhora")).isEqualTo(true);

        // Y el detalle también expone el campo
        ResponseEntity<Map> detalle = rest.getForEntity("/lugares/plaza-mayor-de-ayacucho", Map.class);
        assertThat(detalle.getBody().get("abiertoAhora")).isEqualTo(true);
    }

    @Test
    @Order(4)
    @DisplayName("GET slug inexistente: 404 con mensaje claro")
    void detalleNoExiste() {
        ResponseEntity<Map> res = rest.getForEntity("/lugares/no-existe", Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res.getBody().get("error")).isEqualTo("LUGAR_NO_ENCONTRADO");
    }

    @Test
    @Order(5)
    @DisplayName("POST /lugares sin token: 401; con rol USUARIO: 403 (RNF-16)")
    void escrituraProtegida() {
        ResponseEntity<Map> sinToken = rest.postForEntity("/lugares",
                jsonEntity(cuerpoLugarValido("arco-del-triunfo"), null), Map.class);
        assertThat(sinToken.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        registrarUsuario("editor@test.pe", "Clave1234");
        String tokenUsuario = login("editor@test.pe", "Clave1234");
        ResponseEntity<Map> conUsuario = rest.postForEntity("/lugares",
                jsonEntity(cuerpoLugarValido("arco-del-triunfo"), tokenUsuario), Map.class);
        assertThat(conUsuario.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(6)
    @DisplayName("POST con coordenadas fuera de Ayacucho: 400 (RF-22b)")
    void boundsDeAyacucho() {
        Map<String, Object> fueraDeLima = cuerpoLugarValido("lugar-en-lima");
        fueraDeLima.put("latitud", -12.05);   // Lima
        fueraDeLima.put("longitud", -77.04);

        String tokenAdmin = login("admin@turismohuamanga.pe", "CambiarEnProd_2026!");
        ResponseEntity<Map> res = rest.postForEntity("/lugares",
                jsonEntity(fueraDeLima, tokenAdmin), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().get("error")).isEqualTo("DATOS_INVALIDOS");
    }

    @Test
    @Order(7)
    @DisplayName("ADMIN: crear → 201, actualizar precio → 200, soft delete → 404 público")
    void cicloCompletoAdmin() {
        String tokenAdmin = login("admin@turismohuamanga.pe", "CambiarEnProd_2026!");

        // Crear
        ResponseEntity<Map> creado = rest.postForEntity("/lugares",
                jsonEntity(cuerpoLugarValido("arco-del-triunfo"), tokenAdmin), Map.class);
        assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        idLugarCreado = (String) creado.getBody().get("id");
        assertThat((List<?>) creado.getBody().get("horarios")).hasSize(2);

        // Visible públicamente
        assertThat(rest.getForEntity("/lugares/arco-del-triunfo", Map.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);

        // Actualizar precio (flujo del panel admin → BD, sección 5.5)
        Map<String, Object> cambio = cuerpoLugarValido("arco-del-triunfo");
        cambio.put("precioEntradaPen", 10.50);
        ResponseEntity<Map> actualizado = rest.exchange("/lugares/" + idLugarCreado,
                HttpMethod.PUT, jsonEntity(cambio, tokenAdmin), Map.class);
        assertThat(actualizado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualizado.getBody().get("precioEntradaPen")).isEqualTo(10.50);

        // Soft delete → deja de ser visible
        ResponseEntity<Void> borrado = rest.exchange("/lugares/" + idLugarCreado,
                HttpMethod.DELETE, jsonEntity(null, tokenAdmin), Void.class);
        assertThat(borrado.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(rest.getForEntity("/lugares/arco-del-triunfo", Map.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(8)
    @DisplayName("PUT /auth/password: cambia la clave e invalida las sesiones")
    void cambioDePassword() {
        registrarUsuario("clave@test.pe", "Original88");
        String token = login("clave@test.pe", "Original88");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Void> cambio = rest.exchange("/auth/password", HttpMethod.PUT,
                new HttpEntity<>(Map.of("passwordActual", "Original88",
                        "passwordNueva", "Renovada99"), headers), Void.class);
        assertThat(cambio.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // La clave vieja ya no sirve; la nueva sí
        ResponseEntity<Map> viejaFalla = rest.postForEntity("/auth/login",
                jsonEntity(Map.of("email", "clave@test.pe", "password", "Original88"), null), Map.class);
        assertThat(viejaFalla.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(login("clave@test.pe", "Renovada99")).isNotBlank();
    }

    // ------------------------------------------------------------------

    private Map<String, Object> cuerpoLugarValido(String slug) {
        Map<String, Object> cuerpo = new java.util.HashMap<>();
        cuerpo.put("slug", slug);
        cuerpo.put("categoriaLugarId", "01980000-0000-7000-8000-000000000406"); // CASONAS
        cuerpo.put("distritoId", "01980000-0000-7000-8000-000000000301");      // Ayacucho
        cuerpo.put("latitud", -13.1610);
        cuerpo.put("longitud", -74.2249);
        cuerpo.put("direccion", "Jr. 9 de Diciembre cuadra 2");
        cuerpo.put("precioEntradaPen", 0);
        cuerpo.put("duracionVisitaMin", 15);
        cuerpo.put("aptoNinos", true);
        cuerpo.put("estado", "PUBLICADO");
        cuerpo.put("traducciones", List.of(
                Map.of("idioma", "es", "nombre", "Arco del Triunfo",
                        "descripcion", "Arco conmemorativo de 1910."),
                Map.of("idioma", "en", "nombre", "Arch of Triumph",
                        "descripcion", "Commemorative arch from 1910.")));
        cuerpo.put("horarios", List.of(
                Map.of("diaSemana", 0, "horaApertura", "08:00", "horaCierre", "18:00", "cerrado", false),
                Map.of("diaSemana", 1, "cerrado", true)));
        return cuerpo;
    }

    private HttpEntity<Object> jsonEntity(Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return new HttpEntity<>(body, headers);
    }

    private void registrarUsuario(String email, String password) {
        rest.postForEntity("/auth/register", jsonEntity(
                Map.of("email", email, "password", password, "nombre", "Test"), null), Map.class);
    }

    private String login(String email, String password) {
        ResponseEntity<Map> res = rest.postForEntity("/auth/login", jsonEntity(
                Map.of("email", email, "password", password), null), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) res.getBody().get("accessToken");
    }
}
