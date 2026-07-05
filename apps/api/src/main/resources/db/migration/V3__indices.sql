-- ============================================================================
-- V3: Índices críticos (plan v8.0, sección 6.4 — cumple RNF-30)
-- ============================================================================

-- Lugar
CREATE INDEX idx_lugar_categoria      ON lugar (categoria_lugar_id);          -- RF-04
CREATE INDEX idx_lugar_distrito       ON lugar (distrito_id);                 -- escala regional
CREATE INDEX idx_lugar_ubicacion      ON lugar USING GIST (ubicacion);        -- RF-07, RF-09c
CREATE INDEX idx_lugar_estado_partial ON lugar (estado) WHERE deleted_at IS NULL;

-- HorarioLugar: cálculo "abierto ahora" por lugar+día (RF-09b)
CREATE INDEX idx_horario_lugar_dia ON horario_lugar (lugar_id, dia_semana);

-- Distrito
CREATE INDEX idx_distrito_provincia ON distrito (provincia_id);

-- LugarTraduccion: full-text search (RF-02)
CREATE INDEX idx_lugartrad_fulltext ON lugar_traduccion
    USING GIN (to_tsvector('simple', nombre || ' ' || COALESCE(descripcion, '')));

-- Resena (idx_resena_unique ya existe como UNIQUE constraint en V2)
CREATE INDEX idx_resena_lugar ON resena (lugar_id);

-- Foto: bandeja de moderación (RF-49)
CREATE INDEX idx_foto_pendientes ON foto (created_at) WHERE estado = 'PENDIENTE';

-- ReporteContenido: un reporte por usuario por contenido (UNIQUE parciales)
CREATE UNIQUE INDEX idx_reporte_foto_unique
    ON reporte_contenido (usuario_id, foto_id) WHERE foto_id IS NOT NULL;
CREATE UNIQUE INDEX idx_reporte_resena_unique
    ON reporte_contenido (usuario_id, resena_id) WHERE resena_id IS NOT NULL;

-- Reporte (preservación): mapa de incidentes y moderación
CREATE INDEX idx_reporte_ubicacion ON reporte USING GIST (ubicacion);
CREATE INDEX idx_reporte_estado    ON reporte (estado);

-- Evento: calendario, 'próximos' y 'durante mi visita' (RF-79/84/84b)
CREATE INDEX idx_evento_fecha ON evento (fecha_inicio, fecha_fin);

-- CheckIn: pasaporte y progreso de rutas (RF-39b)
CREATE INDEX idx_checkin_usuario_lugar ON check_in (usuario_id, lugar_id);

-- RefreshToken: limpieza programada
CREATE INDEX idx_refresh_expira ON refresh_token (expira_en);

-- RegistroActividad: listado cronológico admin
CREATE INDEX idx_actividad_created ON registro_actividad (created_at DESC);
