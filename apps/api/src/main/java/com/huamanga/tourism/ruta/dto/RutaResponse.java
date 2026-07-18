package com.huamanga.tourism.ruta.dto;

import java.util.List;
import java.util.UUID;

/** Ruta temática pública (RF-53, contenido por seed) con sus paradas en orden. */
public record RutaResponse(
        UUID id,
        String slug,
        String colorHex,
        String icono,
        String nombre,
        String descripcion,
        int totalLugares,
        Integer duracionEstimadaMin,
        List<ParadaRuta> lugares
) {

    /** Parada del recorrido, en orden de visita. */
    public record ParadaRuta(String slug, String nombre, int orden) {
    }
}
