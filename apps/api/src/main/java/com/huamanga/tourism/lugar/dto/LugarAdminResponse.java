package com.huamanga.tourism.lugar.dto;

import com.huamanga.tourism.lugar.dominio.EstadoLugar;

import java.util.UUID;

/** Fila del listado de gestión del admin: incluye borradores y archivados. */
public record LugarAdminResponse(
        UUID id,
        String slug,
        String nombre,
        String categoriaCodigo,
        EstadoLugar estado
) {
}
