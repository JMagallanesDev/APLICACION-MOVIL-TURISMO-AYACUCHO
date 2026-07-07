package com.huamanga.tourism.evento.dto;

import com.huamanga.tourism.evento.dominio.EstadoEvento;
import com.huamanga.tourism.evento.dominio.TipoEvento;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Ficha completa del evento (RF-80). */
public record EventoDetalleResponse(
        UUID id,
        TipoEvento tipo,
        EstadoEvento estado,
        UUID distritoId,
        UUID lugarId,
        String lugarSlug,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean recurrenteAnual,
        String portadaUrl,
        List<TraduccionEventoDto> traducciones
) {
}
