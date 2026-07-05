package com.huamanga.tourism.lugar.repository;

import com.huamanga.tourism.horario.dominio.HorarioLugar;
import com.huamanga.tourism.horario.repository.HorarioLugarRepository;
import com.huamanga.tourism.lugar.dominio.EstadoLugar;
import com.huamanga.tourism.lugar.dominio.Lugar;
import com.huamanga.tourism.lugar.dominio.LugarTraduccion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración de repositorios contra PostGIS real (Testcontainers).
 * Flyway aplica V1–V6 en el contenedor efímero, por lo que este test valida
 * a la vez: migraciones, mapeo de entidades (ddl-auto: validate) y seed.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class LugarRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4")
                    .asCompatibleSubstituteFor("postgres"));

    @DynamicPropertySource
    static void jpaValidate(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private LugarRepository lugarRepository;

    @Autowired
    private LugarTraduccionRepository lugarTraduccionRepository;

    @Autowired
    private HorarioLugarRepository horarioLugarRepository;

    @Test
    @DisplayName("El seed carga los 5 lugares demo publicados")
    void seedCargaLugaresDemo() {
        assertThat(lugarRepository.count()).isEqualTo(5);
        assertThat(lugarRepository.countByEstadoAndDeletedAtIsNull(EstadoLugar.PUBLICADO)).isEqualTo(5);
    }

    @Test
    @DisplayName("findBySlug recupera la Catedral con sus datos prácticos y coordenadas")
    void findBySlugRecuperaCatedral() {
        Lugar catedral = lugarRepository
                .findBySlugAndDeletedAtIsNull("catedral-de-ayacucho")
                .orElseThrow();

        assertThat(catedral.getDuracionVisitaMin()).isEqualTo(40);
        assertThat(catedral.getAptoNinos()).isTrue();
        // Coordenadas dentro de los bounds de Ayacucho (RF-22b)
        assertThat(catedral.getUbicacion().getX()).isBetween(-75.5, -73.0);
        assertThat(catedral.getUbicacion().getY()).isBetween(-15.5, -12.5);
    }

    @Test
    @DisplayName("Cada lugar demo tiene traducción en español e inglés")
    void lugaresTienenTraduccionesEsEn() {
        lugarRepository.findAll().forEach(lugar -> {
            List<LugarTraduccion> traducciones = lugarTraduccionRepository.findByLugarId(lugar.getId());
            assertThat(traducciones)
                    .as("traducciones de %s", lugar.getSlug())
                    .extracting(LugarTraduccion::getIdioma)
                    .containsExactlyInAnyOrder("es", "en");
        });
    }

    @Test
    @DisplayName("La Catedral tiene turnos partidos y lunes cerrado (caso que motiva HorarioLugar)")
    void catedralTieneTurnosPartidosYLunesCerrado() {
        Lugar catedral = lugarRepository
                .findBySlugAndDeletedAtIsNull("catedral-de-ayacucho")
                .orElseThrow();

        // Domingo (0): dos turnos, mañana y tarde
        List<HorarioLugar> domingo = horarioLugarRepository
                .findByLugarIdAndDiaSemanaOrderByHoraApertura(catedral.getId(), (short) 0);
        assertThat(domingo).hasSize(2);
        assertThat(domingo.get(0).getHoraApertura()).isBefore(domingo.get(1).getHoraApertura());

        // Lunes (1): fila explícita de cerrado
        List<HorarioLugar> lunes = horarioLugarRepository
                .findByLugarIdAndDiaSemanaOrderByHoraApertura(catedral.getId(), (short) 1);
        assertThat(lunes).hasSize(1);
        assertThat(lunes.get(0).isCerrado()).isTrue();
    }
}
