package com.huamanga.tourism.admin.dto;

/**
 * Resumen para el dashboard del admin (RF-52). Los "pendientes" son las colas
 * que requieren acción de moderación (RF-49/50/76).
 */
public record MetricasResponse(
        long lugaresPublicados,
        long usuariosRegistrados,
        long resenasPublicadas,
        long fotosPendientes,
        long resenasEnRevision,
        long reportesRecibidos,
        long reportesAprobados
) {
}
