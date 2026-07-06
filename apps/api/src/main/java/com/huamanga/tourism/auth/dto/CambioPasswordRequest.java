package com.huamanga.tourism.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CambioPasswordRequest(

        @NotBlank(message = "la contraseña actual es obligatoria")
        String passwordActual,

        @NotBlank(message = "la contraseña nueva es obligatoria")
        @Size(min = 8, max = 72, message = "la contraseña debe tener entre 8 y 72 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                 message = "la contraseña debe incluir al menos una letra y un número")
        String passwordNueva
) {
}
