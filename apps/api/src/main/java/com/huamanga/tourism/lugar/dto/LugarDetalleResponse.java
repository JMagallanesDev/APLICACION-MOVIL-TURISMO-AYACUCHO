package com.huamanga.tourism.lugar.dto;

import com.huamanga.tourism.lugar.dominio.EstadoLugar;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Ficha completa (RF-09) con traducciones y horarios estructurados. */
public record LugarDetalleResponse(
        UUID id,
        String slug,
        String categoriaCodigo,
        UUID categoriaLugarId,
        UUID distritoId,
        double latitud,
        double longitud,
        String direccion,
        String telefono,
        BigDecimal precioEntradaPen,
        Integer duracionVisitaMin,
        Boolean aceptaTarjeta,
        Boolean tieneBanos,
        Boolean accesibleSillaRuedas,
        Boolean aptoNinos,
        BigDecimal costoTaxiDesdePlazaPen,
        Boolean requiereGuia,
        EstadoLugar estado,
        List<TraduccionLugarDto> traducciones,
        List<HorarioDto> horarios
) {
}
