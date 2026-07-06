package com.huamanga.tourism.resena.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/** RF-37: calificación 1-5 obligatoria; comentario opcional hasta 500. */
public record ResenaRequest(

        @Min(value = 1, message = "la calificación va de 1 a 5")
        @Max(value = 5, message = "la calificación va de 1 a 5")
        short calificacion,

        @Size(max = 500, message = "el comentario no puede superar 500 caracteres")
        String comentario
) {
}
