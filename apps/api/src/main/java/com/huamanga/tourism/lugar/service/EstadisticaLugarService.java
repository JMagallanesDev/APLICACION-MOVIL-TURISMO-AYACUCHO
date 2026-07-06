package com.huamanga.tourism.lugar.service;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Lectura de la vista materializada estadistica_lugar (sección 6.3): la
 * calificación promedio se lee SIEMPRE de aquí — no existe trigger ni columna
 * de promedio en la tabla lugar (3FN). El refresh corre cada 5 minutos con
 * REFRESH CONCURRENTLY bajo lock distribuido ShedLock.
 * (La vista no se mapea como @Entity: las materialized views no aparecen en
 * los metadatos que valida Hibernate; se consulta vía JDBC.)
 */
@Service
public class EstadisticaLugarService {

    /** Par (promedio, total de reseñas) para un lugar. */
    public record Estadistica(BigDecimal calificacionPromedio, long totalResenas) {
    }

    private static final Logger log = LoggerFactory.getLogger(EstadisticaLugarService.class);

    private final JdbcTemplate jdbc;

    public EstadisticaLugarService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Estadísticas de una página completa de lugares en una sola consulta. */
    public Map<UUID, Estadistica> porLugares(Collection<UUID> lugarIds) {
        Map<UUID, Estadistica> resultado = new HashMap<>();
        if (lugarIds.isEmpty()) {
            return resultado;
        }
        String marcadores = String.join(",", lugarIds.stream().map(x -> "?").toList());
        List<Object> params = List.copyOf(lugarIds);
        jdbc.query(
                "SELECT lugar_id, calificacion_promedio, total_resenas FROM estadistica_lugar "
                        + "WHERE lugar_id IN (" + marcadores + ")",
                rs -> {
                    resultado.put(
                            rs.getObject("lugar_id", UUID.class),
                            new Estadistica(rs.getBigDecimal("calificacion_promedio"),
                                    rs.getLong("total_resenas")));
                },
                params.toArray());
        return resultado;
    }

    /**
     * Refresh cada 5 min (plan 6.3). CONCURRENTLY no bloquea lecturas y exige
     * el índice único idx_estadistica_lugar_pk (creado en V4). ShedLock
     * garantiza una sola ejecución con N instancias.
     */
    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT1M")
    @SchedulerLock(name = "refresh-estadistica-lugar", lockAtLeastFor = "30s")
    public void refrescar() {
        try {
            jdbc.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY estadistica_lugar");
            log.info("estadistica_lugar refrescada");
        } catch (Exception e) {
            log.error("Fallo al refrescar estadistica_lugar: {}", e.getMessage());
        }
    }
}
