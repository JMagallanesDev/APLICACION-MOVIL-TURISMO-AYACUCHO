package com.huamanga.tourism.evento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TraduccionEventoDto(

        @NotBlank @Pattern(regexp = "es|en", message = "idioma soportado: es o en")
        String idioma,

        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 150)
        String nombre,

        String descripcion,

        @Size(max = 150)
        String organizador
) {
}
