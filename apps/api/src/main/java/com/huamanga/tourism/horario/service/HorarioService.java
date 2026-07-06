package com.huamanga.tourism.horario.service;

import com.huamanga.tourism.horario.dominio.HorarioLugar;
import com.huamanga.tourism.horario.repository.HorarioLugarRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Cálculo "abierto/cerrado ahora" (RF-09b) sobre horarios estructurados.
 * Base también para RF-08 (recomendaciones) y RF-29 (planificador).
 */
@Service
public class HorarioService {

    /** Perú no usa horario de verano: zona fija. */
    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    private final HorarioLugarRepository horarioRepository;

    public HorarioService(HorarioLugarRepository horarioRepository) {
        this.horarioRepository = horarioRepository;
    }

    /**
     * Estado por lugar para una página completa (una sola consulta).
     * null = sin horarios registrados (desconocido, distinto de cerrado).
     */
    public Map<UUID, Boolean> abiertosAhora(Collection<UUID> lugarIds) {
        Map<UUID, List<HorarioLugar>> porLugar = horarioRepository.findByLugarIdIn(lugarIds)
                .stream()
                .collect(Collectors.groupingBy(HorarioLugar::getLugarId));

        Map<UUID, Boolean> resultado = new HashMap<>();
        for (UUID id : lugarIds) {
            List<HorarioLugar> horarios = porLugar.get(id);
            resultado.put(id, horarios == null || horarios.isEmpty() ? null : estaAbierto(horarios));
        }
        return resultado;
    }

    /** Evalúa los turnos del día actual; días sin fila cuentan como cerrado. */
    public Boolean estaAbierto(List<HorarioLugar> horariosDelLugar) {
        ZonedDateTime ahora = ZonedDateTime.now(ZONA_LIMA);
        // DayOfWeek: MONDAY=1..SUNDAY=7 → nuestro esquema: 0=domingo..6=sábado
        short diaHoy = (short) (ahora.getDayOfWeek().getValue() % 7);
        LocalTime hora = ahora.toLocalTime();

        return horariosDelLugar.stream()
                .filter(h -> h.getDiaSemana() == diaHoy)
                .anyMatch(h -> !h.isCerrado()
                        && h.getHoraApertura() != null && h.getHoraCierre() != null
                        && !hora.isBefore(h.getHoraApertura())
                        && hora.isBefore(h.getHoraCierre()));
    }
}
