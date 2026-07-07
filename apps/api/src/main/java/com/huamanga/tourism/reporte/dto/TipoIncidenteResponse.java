package com.huamanga.tourism.reporte.dto;

import java.util.UUID;

/** Tipo de incidente con el nombre resuelto al idioma pedido (RF-70). */
public record TipoIncidenteResponse(UUID id, String codigo, String nombre,
                                    String icono, String colorHex) {
}
