package com.huamanga.tourism.evento.dto;

import com.huamanga.tourism.evento.dominio.EstadoEvento;
import com.huamanga.tourism.evento.dominio.TipoEvento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Alta/edición de evento (RF-86). El contenido vive en BD, gestionado por admin. */
public record EventoRequest(

        @NotNull(message = "el tipo es obligatorio")
        TipoEvento tipo,

        @NotNull(message = "el distrito es obligatorio")
        UUID distritoId,

        UUID lugarId, // opcional

        String cloudinaryUrlPortada,

        @NotNull(message = "la fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "la fecha de fin es obligatoria")
        LocalDate fechaFin,

        boolean recurrenteAnual,

        @NotNull(message = "el estado es obligatorio")
        EstadoEvento estado,

        @NotEmpty(message = "se requiere al menos la traducción en español")
        List<@Valid TraduccionEventoDto> traducciones
) {

    @AssertTrue(message = "la fecha de fin no puede ser anterior a la de inicio")
    public boolean isRangoValido() {
        return fechaInicio == null || fechaFin == null || !fechaFin.isBefore(fechaInicio);
    }

    @AssertTrue(message = "la traducción en español (es) es obligatoria")
    public boolean isConEspanol() {
        return traducciones != null && traducciones.stream().anyMatch(t -> "es".equals(t.idioma()));
    }
}
