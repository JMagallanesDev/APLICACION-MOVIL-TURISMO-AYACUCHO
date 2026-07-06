package com.huamanga.tourism.recomendacion.service;

import com.huamanga.tourism.clima.dto.ClimaResponse;
import com.huamanga.tourism.clima.service.ClimaService;
import com.huamanga.tourism.horario.service.HorarioService;
import com.huamanga.tourism.lugar.dominio.CategoriaLugar;
import com.huamanga.tourism.lugar.dominio.EstadoLugar;
import com.huamanga.tourism.lugar.dominio.Lugar;
import com.huamanga.tourism.lugar.dominio.LugarTraduccion;
import com.huamanga.tourism.lugar.repository.CategoriaLugarRepository;
import com.huamanga.tourism.lugar.repository.LugarRepository;
import com.huamanga.tourism.lugar.repository.LugarTraduccionRepository;
import com.huamanga.tourism.recomendacion.dto.RecomendacionResponse;
import com.huamanga.tourism.recomendacion.dto.RecomendacionResponse.LugarRecomendado;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Motor de reglas "¿Qué hago ahora?" (RF-08): cruza hora actual de Lima +
 * clima (caché Redis) + estado abierto/cerrado + categoría. Sin cambios en BD.
 * Si el clima no está disponible, degrada a reglas de franja horaria.
 */
@Service
public class RecomendacionService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    private final LugarRepository lugarRepository;
    private final LugarTraduccionRepository traduccionRepository;
    private final CategoriaLugarRepository categoriaRepository;
    private final HorarioService horarioService;
    private final ClimaService climaService;

    public RecomendacionService(LugarRepository lugarRepository,
                                LugarTraduccionRepository traduccionRepository,
                                CategoriaLugarRepository categoriaRepository,
                                HorarioService horarioService,
                                ClimaService climaService) {
        this.lugarRepository = lugarRepository;
        this.traduccionRepository = traduccionRepository;
        this.categoriaRepository = categoriaRepository;
        this.horarioService = horarioService;
        this.climaService = climaService;
    }

    @Transactional(readOnly = true)
    public RecomendacionResponse ahora(String idioma, int limite) {
        String franja = franjaActual();

        ClimaResponse clima;
        try {
            clima = climaService.actual(idioma);
        } catch (Exception e) {
            clima = null; // degradación: reglas solo por franja horaria
        }

        boolean lluvia = clima != null && clima.lluvia();
        List<String> preferidas = categoriasPreferidas(franja, lluvia);
        String razonPreferida = lluvia ? "LLUVIA_TECHADO" : "IDEAL_" + franja;

        List<Lugar> publicados = lugarRepository
                .findByEstadoAndDeletedAtIsNull(EstadoLugar.PUBLICADO, PageRequest.of(0, 100))
                .getContent();
        List<UUID> ids = publicados.stream().map(Lugar::getId).toList();

        Map<UUID, Boolean> abiertos = horarioService.abiertosAhora(ids);
        Map<UUID, String> categorias = categoriaRepository.findAll().stream()
                .collect(Collectors.toMap(CategoriaLugar::getId, CategoriaLugar::getCodigo));
        Map<UUID, LugarTraduccion> nombres = traduccionRepository.findByLugarIdIn(ids).stream()
                .collect(Collectors.toMap(LugarTraduccion::getLugarId, Function.identity(),
                        (a, b) -> idioma.equals(a.getIdioma()) ? a : b));

        List<LugarRecomendado> resultado = new ArrayList<>();

        // 1º: abiertos de las categorías preferidas por clima/franja
        for (Lugar lugar : publicados) {
            String categoria = categorias.get(lugar.getCategoriaLugarId());
            if (Boolean.TRUE.equals(abiertos.get(lugar.getId())) && preferidas.contains(categoria)) {
                resultado.add(recomendado(lugar, categoria, true, razonPreferida, nombres));
            }
        }
        // 2º: cualquier otro lugar abierto ahora
        for (Lugar lugar : publicados) {
            if (resultado.size() >= limite) {
                break;
            }
            String categoria = categorias.get(lugar.getCategoriaLugarId());
            boolean yaIncluido = resultado.stream().anyMatch(r -> r.id().equals(lugar.getId()));
            if (!yaIncluido && Boolean.TRUE.equals(abiertos.get(lugar.getId()))) {
                resultado.add(recomendado(lugar, categoria, true, "ABIERTO_AHORA", nombres));
            }
        }

        return new RecomendacionResponse(franja, clima,
                resultado.stream().limit(limite).toList());
    }

    // ------------------------------------------------------------------

    private LugarRecomendado recomendado(Lugar lugar, String categoria, boolean abierto,
                                         String razon, Map<UUID, LugarTraduccion> nombres) {
        LugarTraduccion traduccion = nombres.get(lugar.getId());
        return new LugarRecomendado(
                lugar.getId(),
                lugar.getSlug(),
                traduccion != null ? traduccion.getNombre() : lugar.getSlug(),
                categoria,
                abierto,
                razon);
    }

    private String franjaActual() {
        int hora = ZonedDateTime.now(ZONA_LIMA).getHour();
        if (hora < 6) return "MADRUGADA";
        if (hora < 11) return "MANANA";
        if (hora < 14) return "MEDIODIA";
        if (hora < 18) return "TARDE";
        return "NOCHE";
    }

    private List<String> categoriasPreferidas(String franja, boolean lluvia) {
        if (lluvia) {
            // Con lluvia: lugares techados
            return List.of("MUSEOS", "IGLESIAS", "CASONAS", "TALLERES_ARTESANALES");
        }
        return switch (franja) {
            case "MANANA" -> List.of("MIRADORES", "PLAZAS", "SITIOS_ARQUEOLOGICOS", "NATURALEZA");
            case "MEDIODIA" -> List.of("MUSEOS", "CASONAS", "TALLERES_ARTESANALES");
            case "TARDE" -> List.of("MIRADORES", "IGLESIAS", "PLAZAS");
            case "NOCHE" -> List.of("PLAZAS", "CASONAS");
            default -> List.of("PLAZAS"); // MADRUGADA
        };
    }
}
