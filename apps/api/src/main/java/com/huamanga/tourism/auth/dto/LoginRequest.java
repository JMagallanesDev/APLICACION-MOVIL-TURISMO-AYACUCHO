package com.huamanga.tourism.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "el email es obligatorio")
        @Email(message = "debe ser un email válido")
        String email,

        @NotBlank(message = "la contraseña es obligatoria")
        String password
) {
}
