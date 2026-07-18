package com.huamanga.tourism.ruta.service;

import com.huamanga.tourism.lugar.dominio.EstadoLugar;
import com.huamanga.tourism.lugar.dominio.Lugar;
import com.huamanga.tourism.lugar.dominio.LugarTraduccion;
import com.huamanga.tourism.lugar.repository.LugarRepository;
import com.huamanga.tourism.lugar.repository.LugarTraduccionRepository;
import com.huamanga.tourism.ruta.dominio.LugarRuta;
import com.huamanga.tourism.ruta.dominio.RutaTematica;
import com.huamanga.tourism.ruta.dominio.RutaTraduccion;
import com.huamanga.tourism.ruta.repository.LugarRutaRepository;
import com.huamanga.tourism.ruta.repository.RutaTematicaRepository;
import com.huamanga.tourism.ruta.repository.RutaTraduccionRepository;
import com.huamanga.tourism.ruta.dto.RutaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Rutas temáticas de solo lectura (RF-53 recortado: contenido por seed,
 * sin CRUD). Cada ruta lista sus paradas publicadas en orden de visita.
 */
@Service
public class RutaService {

    private final RutaTematicaRepository rutaRepository;
    private final RutaTraduccionRepository traduccionRepository;
    private final LugarRutaRepository lugarRutaRepository;
    private final LugarRepository lugarRepository;
    private final LugarTraduccionRepository lugarTraduccionRepository;

    public RutaService(RutaTematicaRepository rutaRepository,
                       RutaTraduccionRepository traduccionRepository,
                       LugarRutaRepository lugarRutaRepository,
                       LugarRepository lugarRepository,
                       LugarTraduccionRepository lugarTraduccionRepository) {
        this.rutaRepository = rutaRepository;
        this.traduccionRepository = traduccionRepository;
        this.lugarRutaRepository = lugarRutaRepository;
        this.lugarRepository = lugarRepository;
        this.lugarTraduccionRepository = lugarTraduccionRepository;
    }

    @Transactional(readOnly = true)
    public List<RutaResponse> listar(String idioma) {
        List<RutaTematica> rutas = rutaRepository.findByActivaTrueAndDeletedAtIsNullOrderByOrdenAsc();
        if (rutas.isEmpty()) {
            return List.of();
        }
        List<UUID> rutaIds = rutas.stream().map(RutaTematica::getId).toList();

        Map<UUID, RutaTraduccion> traducciones =
                traduccionRepository.findByRutaTematicaIdIn(rutaIds).stream()
                        .collect(Collectors.toMap(RutaTraduccion::getRutaTematicaId,
                                Function.identity(),
                                (a, b) -> idioma.equals(a.getIdioma()) ? a : b));

        Map<UUID, List<LugarRuta>> paradasPorRuta =
                lugarRutaRepository.findByRutaTematicaIdInOrderByOrdenAsc(rutaIds).stream()
                        .collect(Collectors.groupingBy(LugarRuta::getRutaTematicaId));

        List<UUID> lugarIds = paradasPorRuta.values().stream()
                .flatMap(List::stream).map(LugarRuta::getLugarId).distinct().toList();
        Map<UUID, Lugar> lugares = lugarRepository.findAllById(lugarIds).stream()
                .filter(l -> l.getDeletedAt() == null && l.getEstado() == EstadoLugar.PUBLICADO)
                .collect(Collectors.toMap(Lugar::getId, Function.identity()));
        Map<UUID, String> nombresLugar = lugarTraduccionRepository.findByLugarIdIn(lugarIds).stream()
                .collect(Collectors.toMap(LugarTraduccion::getLugarId, LugarTraduccion::getNombre,
                        (a, b) -> a));

        return rutas.stream().map(ruta -> {
            RutaTraduccion trad = traducciones.get(ruta.getId());
            List<RutaResponse.ParadaRuta> paradas = paradasPorRuta
                    .getOrDefault(ruta.getId(), List.of()).stream()
                    .filter(p -> lugares.containsKey(p.getLugarId()))
                    .map(p -> new RutaResponse.ParadaRuta(
                            lugares.get(p.getLugarId()).getSlug(),
                            nombresLugar.getOrDefault(p.getLugarId(),
                                    lugares.get(p.getLugarId()).getSlug()),
                            p.getOrden()))
                    .toList();
            int duracion = paradasPorRuta.getOrDefault(ruta.getId(), List.of()).stream()
                    .map(p -> lugares.get(p.getLugarId()))
                    .filter(l -> l != null && l.getDuracionVisitaMin() != null)
                    .mapToInt(Lugar::getDuracionVisitaMin)
                    .sum();
            return new RutaResponse(
                    ruta.getId(), ruta.getSlug(), ruta.getColorHex(), ruta.getIcono(),
                    trad != null ? trad.getNombre() : ruta.getSlug(),
                    trad != null ? trad.getDescripcion() : null,
                    paradas.size(),
                    duracion > 0 ? duracion : null,
                    paradas);
        }).toList();
    }
}
