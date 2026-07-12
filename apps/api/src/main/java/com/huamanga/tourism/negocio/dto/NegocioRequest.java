package com.huamanga.tourism.negocio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Registro/edición del negocio propio (RF-104/107). El WhatsApp es
 * obligatorio: es el canal de contacto del directorio (RF-110); la
 * transacción ocurre fuera del sistema (alcance excluido 4.3).
 */
public record NegocioRequest(

        @NotNull(message = "la categoría es obligatoria")
        UUID categoriaNegocioId,

        @NotBlank(message = "el nombre del negocio es obligatorio")
        @Size(max = 150)
        String nombre,

        @Pattern(regexp = "\\d{11}", message = "el RUC debe tener 11 dígitos")
        String ruc,

        @Size(max = 30)
        String telefono,

        @NotBlank(message = "el número de WhatsApp es obligatorio")
        @Pattern(regexp = "\\+?\\d{9,15}", message = "WhatsApp inválido: usa solo dígitos (ej. 51966123456)")
        String whatsapp,

        @Size(max = 255)
        String direccion,

        @Size(max = 255)
        String horario,

        @NotBlank(message = "la descripción en español es obligatoria")
        String descripcionEs,

        String descripcionEn
) {
}
