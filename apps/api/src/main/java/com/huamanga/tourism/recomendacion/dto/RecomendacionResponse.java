package com.huamanga.tourism.recomendacion.dto;

import com.huamanga.tourism.clima.dto.ClimaResponse;

import java.util.List;
import java.util.UUID;

/**
 * Respuesta de "¿Qué hago ahora?" (RF-08): franja horaria + clima (nullable
 * si el proveedor no está disponible) + lugares sugeridos con la razón como
 * clave i18n que el frontend traduce.
 */
public record RecomendacionResponse(
        String franja,
        ClimaResponse clima,
        List<LugarRecomendado> lugares
) {

    public record LugarRecomendado(
            UUID id,
            String slug,
            String nombre,
            String categoriaCodigo,
            Boolean abiertoAhora,
            String razonClave
    ) {
    }
}
