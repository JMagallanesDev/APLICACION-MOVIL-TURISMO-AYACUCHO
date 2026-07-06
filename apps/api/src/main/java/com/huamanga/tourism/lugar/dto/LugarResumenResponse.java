package com.huamanga.tourism.lugar.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Card del listado (RF-01): nombre ya resuelto al idioma solicitado. */
public record LugarResumenResponse(
        UUID id,
        String slug,
        String nombre,
        String descripcion,
        String categoriaCodigo,
        double latitud,
        double longitud,
        BigDecimal precioEntradaPen,
        Integer duracionVisitaMin
) {
}
