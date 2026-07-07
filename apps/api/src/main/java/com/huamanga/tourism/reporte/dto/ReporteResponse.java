package com.huamanga.tourism.reporte.dto;

import com.huamanga.tourism.reporte.dominio.EstadoReporte;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Reporte devuelto tras crear o en moderación. En el mapa público NUNCA se
 * expone nombre del reportante (aunque no sea anónimo, se omite por defecto).
 */
public record ReporteResponse(
        UUID id,
        String tipoIncidenteCodigo,
        String descripcion,
        double latitud,
        double longitud,
        String direccionReferencial,
        EstadoReporte estado,
        boolean esAnonimo,
        List<String> fotos,
        Instant creadoEn
) {
}
