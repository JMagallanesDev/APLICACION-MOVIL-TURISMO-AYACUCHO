package com.huamanga.tourism.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** RF-31: registro con email + contraseña validada. */
public record RegisterRequest(

        @NotBlank(message = "el email es obligatorio")
        @Email(message = "debe ser un email válido")
        String email,

        @NotBlank(message = "la contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "la contraseña debe tener entre 8 y 72 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                 message = "la contraseña debe incluir al menos una letra y un número")
        String password,

        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 120, message = "el nombre no puede superar 120 caracteres")
        String nombre
) {
}
