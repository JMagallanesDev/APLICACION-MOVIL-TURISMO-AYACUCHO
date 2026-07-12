package com.huamanga.tourism.negocio.dto;

import com.huamanga.tourism.negocio.dominio.EstadoNegocio;

import java.util.UUID;

/**
 * Negocio del directorio. estado solo es relevante para el dueño y el admin;
 * en el listado público siempre es APROBADO.
 */
public record NegocioResponse(
        UUID id,
        String nombre,
        String categoriaCodigo,
        String descripcion,
        String whatsapp,
        String telefono,
        String direccion,
        String horario,
        EstadoNegocio estado
) {
}
