-- ============================================================================
-- V4: Vista materializada EstadisticaLugar (plan v8.0, sección 6.3)
-- Mantiene las tablas base en 3FN sin sacrificar performance de rankings
-- (RF-06). Se refresca cada 5 min vía Spring Scheduler + ShedLock con
-- REFRESH MATERIALIZED VIEW CONCURRENTLY (requiere el índice único).
-- La calificación promedio se lee SIEMPRE de aquí: no existe trigger ni
-- columna de promedio en la tabla lugar.
-- ============================================================================

CREATE MATERIALIZED VIEW estadistica_lugar AS
SELECT
    l.id                                          AS lugar_id,
    COALESCE(r.calificacion_promedio, 0)::numeric(3,2) AS calificacion_promedio,
    COALESCE(r.total_resenas, 0)                  AS total_resenas,
    COALESCE(c.total_visitas, 0)                  AS total_visitas,
    COALESCE(f.total_favoritos, 0)                AS total_favoritos,
    now()                                         AS actualizado_en
FROM lugar l
LEFT JOIN (
    SELECT lugar_id, AVG(calificacion) AS calificacion_promedio, COUNT(*) AS total_resenas
    FROM resena
    WHERE estado = 'PUBLICADA'
    GROUP BY lugar_id
) r ON r.lugar_id = l.id
LEFT JOIN (
    SELECT lugar_id, COUNT(*) AS total_visitas
    FROM check_in
    GROUP BY lugar_id
) c ON c.lugar_id = l.id
LEFT JOIN (
    SELECT lugar_id, COUNT(*) AS total_favoritos
    FROM favorito
    GROUP BY lugar_id
) f ON f.lugar_id = l.id
WHERE l.deleted_at IS NULL;

-- Obligatorio para REFRESH MATERIALIZED VIEW CONCURRENTLY
CREATE UNIQUE INDEX idx_estadistica_lugar_pk ON estadistica_lugar (lugar_id);

-- Rankings (RF-06)
CREATE INDEX idx_estadistica_calificacion ON estadistica_lugar (calificacion_promedio DESC);
CREATE INDEX idx_estadistica_visitas      ON estadistica_lugar (total_visitas DESC);
