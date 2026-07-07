package com.huamanga.tourism.evento.dto;

import com.huamanga.tourism.evento.dominio.TipoEvento;

import java.time.LocalDate;
import java.util.UUID;

/** Card de la agenda: nombre ya resuelto al idioma pedido. */
public record EventoResumenResponse(
        UUID id,
        TipoEvento tipo,
        String nombre,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String portadaUrl
) {
}
