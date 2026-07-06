package com.huamanga.tourism.lugar.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Card del listado (RF-01): nombre ya resuelto al idioma solicitado.
 * abiertoAhora (RF-09b): true/false calculado con hora de Lima sobre
 * HorarioLugar; null si el lugar no tiene horarios registrados.
 */
public record LugarResumenResponse(
        UUID id,
        String slug,
        String nombre,
        String descripcion,
        String categoriaCodigo,
        double latitud,
        double longitud,
        BigDecimal precioEntradaPen,
        Integer duracionVisitaMin,
        Boolean abiertoAhora,
        /** De la vista materializada (RF-06); null si aún no hay refresh. */
        BigDecimal calificacionPromedio,
        Long totalResenas
) {
}
