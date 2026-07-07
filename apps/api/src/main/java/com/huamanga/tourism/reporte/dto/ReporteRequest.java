package com.huamanga.tourism.reporte.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Reporte ciudadano (RF-69/71/72). Coordenadas dentro de los bounds de
 * Ayacucho (RF-22b). El reporte anónimo no exige nombre; el identificado
 * puede dar uno (nunca se guarda IP).
 */
public record ReporteRequest(

        @NotNull(message = "el tipo de incidente es obligatorio")
        UUID tipoIncidenteId,

        @NotBlank(message = "describe brevemente el incidente")
        @Size(max = 2000, message = "la descripción no puede superar 2000 caracteres")
        String descripcion,

        @NotNull(message = "la latitud es obligatoria")
        @DecimalMin(value = "-15.5", message = "ubicación fuera de la región Ayacucho")
        @DecimalMax(value = "-12.5", message = "ubicación fuera de la región Ayacucho")
        Double latitud,

        @NotNull(message = "la longitud es obligatoria")
        @DecimalMin(value = "-75.5", message = "ubicación fuera de la región Ayacucho")
        @DecimalMax(value = "-73.0", message = "ubicación fuera de la región Ayacucho")
        Double longitud,

        @Size(max = 255)
        String direccionReferencial,

        boolean esAnonimo,

        @Size(max = 120)
        String nombreReportante
) {
}
