package com.huamanga.tourism.lugar.mapper;

import com.huamanga.tourism.horario.dominio.HorarioLugar;
import com.huamanga.tourism.lugar.dominio.Lugar;
import com.huamanga.tourism.lugar.dominio.LugarTraduccion;
import com.huamanga.tourism.lugar.dto.HorarioDto;
import com.huamanga.tourism.lugar.dto.LugarDetalleResponse;
import com.huamanga.tourism.lugar.dto.LugarResumenResponse;
import com.huamanga.tourism.lugar.dto.TraduccionLugarDto;
import com.huamanga.tourism.lugar.service.EstadisticaLugarService.Estadistica;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Conversión Entity ⇄ DTO: las entidades jamás salen del API (sección 10.2).
 * Mapper manual explícito; migrable a MapStruct sin tocar contratos.
 */
@Component
public class LugarMapper {

    public LugarResumenResponse aResumen(Lugar lugar, LugarTraduccion traduccion,
                                         String categoriaCodigo, Boolean abiertoAhora,
                                         Estadistica estadistica) {
        return new LugarResumenResponse(
                lugar.getId(),
                lugar.getSlug(),
                traduccion != null ? traduccion.getNombre() : lugar.getSlug(),
                traduccion != null ? traduccion.getDescripcion() : null,
                categoriaCodigo,
                lugar.getUbicacion().getY(),
                lugar.getUbicacion().getX(),
                lugar.getPrecioEntradaPen(),
                lugar.getDuracionVisitaMin(),
                abiertoAhora,
                estadistica != null ? estadistica.calificacionPromedio() : null,
                estadistica != null ? estadistica.totalResenas() : null);
    }

    public LugarDetalleResponse aDetalle(Lugar lugar, String categoriaCodigo,
                                         Boolean abiertoAhora, Estadistica estadistica,
                                         List<LugarTraduccion> traducciones,
                                         List<HorarioLugar> horarios) {
        return new LugarDetalleResponse(
                lugar.getId(),
                lugar.getSlug(),
                categoriaCodigo,
                lugar.getCategoriaLugarId(),
                lugar.getDistritoId(),
                lugar.getUbicacion().getY(),
                lugar.getUbicacion().getX(),
                lugar.getDireccion(),
                lugar.getTelefono(),
                lugar.getPrecioEntradaPen(),
                lugar.getDuracionVisitaMin(),
                lugar.getAceptaTarjeta(),
                lugar.getTieneBanos(),
                lugar.getAccesibleSillaRuedas(),
                lugar.getAptoNinos(),
                lugar.getCostoTaxiDesdePlazaPen(),
                lugar.getRequiereGuia(),
                lugar.getEstado(),
                abiertoAhora,
                estadistica != null ? estadistica.calificacionPromedio() : null,
                estadistica != null ? estadistica.totalResenas() : null,
                traducciones.stream()
                        .map(t -> new TraduccionLugarDto(t.getIdioma(), t.getNombre(),
                                t.getDescripcion(), t.getHistoria(), t.getConsejos()))
                        .toList(),
                horarios.stream()
                        .map(h -> new HorarioDto(h.getDiaSemana(), h.getHoraApertura(),
                                h.getHoraCierre(), h.isCerrado()))
                        .toList());
    }
}
