package com.huamanga.tourism.resena.dto;

import java.time.Instant;
import java.util.UUID;

/** Reseña pública: solo el nombre del autor, jamás su email (sección 10.2). */
public record ResenaResponse(
        UUID id,
        short calificacion,
        String comentario,
        String autorNombre,
        Instant creadaEn
) {
}
