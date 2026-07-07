package com.huamanga.tourism.reporte.dto;

import com.huamanga.tourism.reporte.dominio.EstadoReporte;

import java.util.UUID;

/**
 * Punto del mapa público de incidentes (RF-74): mínimo indispensable, sin
 * datos del reportante. Solo se exponen incidentes aprobados o resueltos.
 */
public record IncidenteMapaResponse(
        UUID id,
        String tipoIncidenteCodigo,
        String icono,
        String colorHex,
        double latitud,
        double longitud,
        EstadoReporte estado
) {
}
