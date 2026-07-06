package com.huamanga.tourism.lugar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TraduccionLugarDto(

        @NotBlank(message = "el idioma es obligatorio")
        @Pattern(regexp = "es|en", message = "idioma soportado: es o en")
        String idioma,

        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 150, message = "el nombre no puede superar 150 caracteres")
        String nombre,

        String descripcion,
        String historia,
        String consejos
) {
}
