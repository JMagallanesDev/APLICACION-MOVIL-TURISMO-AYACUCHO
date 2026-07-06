package com.huamanga.tourism.foto.dto;

import com.huamanga.tourism.foto.dominio.EstadoFoto;

import java.time.Instant;
import java.util.UUID;

/** Foto de la galería; estado y motivo solo son relevantes para autor/admin. */
public record FotoResponse(
        UUID id,
        String url,
        EstadoFoto estado,
        String motivoRechazo,
        String autorNombre,
        Instant creadaEn
) {
}
