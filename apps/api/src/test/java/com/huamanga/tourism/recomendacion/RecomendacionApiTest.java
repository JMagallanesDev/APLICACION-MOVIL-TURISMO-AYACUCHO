package com.huamanga.tourism.recomendacion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
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
 * RF-08 y RF-25 sin OPENWEATHER_API_KEY configurada: las recomendaciones
 * degradan a reglas de franja horaria (clima null, nunca error) y el clima
 * declara no-disponible con mensaje claro. Degradación elegante (5.4).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RecomendacionApiTest {

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
    @DisplayName("GET /recomendaciones/ahora: 200 público, franja válida y clima null sin API key")
    void recomendacionesDegradanSinClima() {
        ResponseEntity<Map> res = rest.getForEntity("/recomendaciones/ahora?limite=4", Map.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().get("franja"))
                .isIn("MADRUGADA", "MANANA", "MEDIODIA", "TARDE", "NOCHE");
        assertThat(res.getBody().get("clima")).isNull();

        List<Map<String, Object>> lugares = (List<Map<String, Object>>) res.getBody().get("lugares");
        assertThat(lugares).isNotNull();
        assertThat(lugares.size()).isLessThanOrEqualTo(4);
        // Cada sugerencia trae slug, nombre y una razón traducible
        lugares.forEach(l -> {
            assertThat(l.get("slug")).isNotNull();
            assertThat(l.get("nombre")).isNotNull();
            assertThat((String) l.get("razonClave")).isNotBlank();
            assertThat(l.get("abiertoAhora")).isEqualTo(true); // solo se sugiere lo abierto
        });
    }

    @Test
    @DisplayName("GET /clima/actual sin API key: 503 con código claro (RNF-23)")
    void climaNoDisponibleSinApiKey() {
        ResponseEntity<Map> res = rest.getForEntity("/clima/actual", Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(res.getBody().get("error")).isEqualTo("CLIMA_NO_DISPONIBLE");
    }
}
