package com.huamanga.tourism.reporte;

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
 * Preservación ciudadana (diferenciador): reporte anónimo sin cuenta ni IP,
 * validación de bounds de Ayacucho, mapa público solo con aprobados, y
 * moderación que vuelve visible un incidente. El tipo de incidente se toma
 * del seed V5.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReporteApiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4")
                    .asCompatibleSubstituteFor("postgres"));

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    // VANDALISMO (seed V5)
    private static final String TIPO_VANDALISMO = "01980000-0000-7000-8000-000000000601";

    @Autowired
    private TestRestTemplate rest;

    private static String reporteId;

    @Test
    @Order(1)
    @DisplayName("GET /tipos-incidente público: 7 tipos traducidos (RF-70)")
    void tiposDeIncidente() {
        ResponseEntity<List> es = rest.getForEntity("/tipos-incidente?idioma=es", List.class);
        assertThat(es.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(es.getBody()).hasSize(7);

        ResponseEntity<List> en = rest.getForEntity("/tipos-incidente?idioma=en", List.class);
        List<Map<String, Object>> tipos = en.getBody();
        assertThat(tipos).anyMatch(t -> "Vandalism".equals(t.get("nombre")));
    }

    @Test
    @Order(2)
    @DisplayName("POST /reportes anónimo sin cuenta: 201, estado RECIBIDO (RF-69/72)")
    void reporteAnonimo() {
        ResponseEntity<Map> res = crearReporte(Map.of(
                "tipoIncidenteId", TIPO_VANDALISMO,
                "descripcion", "Pintas sobre el muro de la Catedral.",
                "latitud", -13.1635,
                "longitud", -74.2232,
                "esAnonimo", true));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getBody().get("estado")).isEqualTo("RECIBIDO");
        assertThat(res.getBody().get("esAnonimo")).isEqualTo(true);
        reporteId = (String) res.getBody().get("id");
    }

    @Test
    @Order(3)
    @DisplayName("POST con coordenadas fuera de Ayacucho: 400 (RF-22b)")
    void fueraDeAyacucho() {
        ResponseEntity<Map> res = crearReporte(Map.of(
                "tipoIncidenteId", TIPO_VANDALISMO,
                "descripcion", "Prueba fuera de la región.",
                "latitud", -12.05, // Lima
                "longitud", -77.04,
                "esAnonimo", true));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().get("error")).isEqualTo("DATOS_INVALIDOS");
    }

    @Test
    @Order(4)
    @DisplayName("Mapa público NO muestra reportes RECIBIDO; sí tras aprobar (RF-74/76)")
    void mapaSoloAprobados() {
        // Recién creado (RECIBIDO) no aparece en el mapa público
        assertThat(mapaPublico()).noneMatch(i -> reporteId.equals(i.get("id")));

        // ADMIN aprueba el reporte
        String tokenAdmin = loginAdmin();
        ResponseEntity<Void> aprobado = rest.exchange("/admin/moderacion/reportes/" + reporteId,
                HttpMethod.PATCH, jsonAuth(Map.of("estado", "APROBADO",
                        "notasAdmin", "Verificado en campo."), tokenAdmin), Void.class);
        assertThat(aprobado.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Ahora sí aparece, sin exponer datos del reportante
        List<Map<String, Object>> mapa = mapaPublico();
        assertThat(mapa).anyMatch(i -> reporteId.equals(i.get("id")));
        assertThat(mapa.get(0)).doesNotContainKeys("nombreReportante", "descripcion");
    }

    @Test
    @Order(5)
    @DisplayName("Bandeja de moderación exige ADMIN: anónimo 401 (RF-76)")
    void bandejaSoloAdmin() {
        assertThat(rest.getForEntity("/admin/moderacion/reportes", Map.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ------------------------------------------------------------------

    private ResponseEntity<Map> crearReporte(Map<String, Object> datos) {
        HttpHeaders jsonPart = new HttpHeaders();
        jsonPart.setContentType(MediaType.APPLICATION_JSON);
        MultiValueMap<String, Object> cuerpo = new LinkedMultiValueMap<>();
        cuerpo.add("datos", new HttpEntity<>(datos, jsonPart));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return rest.postForEntity("/reportes", new HttpEntity<>(cuerpo, headers), Map.class);
    }

    private List<Map<String, Object>> mapaPublico() {
        return rest.getForEntity("/reportes/mapa", List.class).getBody();
    }

    private HttpEntity<Object> jsonAuth(Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    private String loginAdmin() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> res = rest.postForEntity("/auth/login",
                new HttpEntity<>(Map.of("email", "admin@turismohuamanga.pe",
                        "password", "CambiarEnProd_2026!"), headers), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) res.getBody().get("accessToken");
    }
}
