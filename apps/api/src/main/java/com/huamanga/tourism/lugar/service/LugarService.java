package com.huamanga.tourism.lugar.service;

import com.huamanga.tourism.common.exception.NegocioException;
import com.huamanga.tourism.common.integration.RevalidacionIsrService;
import com.huamanga.tourism.common.security.UsuarioAutenticado;
import com.huamanga.tourism.geografia.repository.DistritoRepository;
import com.huamanga.tourism.horario.dominio.HorarioLugar;
import com.huamanga.tourism.horario.repository.HorarioLugarRepository;
import com.huamanga.tourism.horario.service.HorarioService;
import com.huamanga.tourism.lugar.dominio.CategoriaLugar;
import com.huamanga.tourism.lugar.dominio.EstadoLugar;
import com.huamanga.tourism.lugar.dominio.Lugar;
import com.huamanga.tourism.lugar.dominio.LugarTraduccion;
import com.huamanga.tourism.lugar.dto.LugarDetalleResponse;
import com.huamanga.tourism.lugar.dto.LugarRequest;
import com.huamanga.tourism.lugar.dto.LugarResumenResponse;
import com.huamanga.tourism.lugar.mapper.LugarMapper;
import com.huamanga.tourism.lugar.repository.CategoriaLugarRepository;
import com.huamanga.tourism.lugar.repository.LugarRepository;
import com.huamanga.tourism.lugar.repository.LugarTraduccionRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lógica de negocio del catálogo de lugares (RF-47). El contenido editorial
 * vive SIEMPRE en BD y se gestiona desde aquí, nunca hardcodeado (sección 5.5).
 */
@Service
public class LugarService {

    private static final GeometryFactory GEOMETRIAS =
            new GeometryFactory(new PrecisionModel(), 4326);

    private final LugarRepository lugarRepository;
    private final LugarTraduccionRepository traduccionRepository;
    private final HorarioLugarRepository horarioRepository;
    private final CategoriaLugarRepository categoriaRepository;
    private final DistritoRepository distritoRepository;
    private final LugarMapper mapper;
    private final RevalidacionIsrService revalidacion;
    private final HorarioService horarioService;

    public LugarService(LugarRepository lugarRepository,
                        LugarTraduccionRepository traduccionRepository,
                        HorarioLugarRepository horarioRepository,
                        CategoriaLugarRepository categoriaRepository,
                        DistritoRepository distritoRepository,
                        LugarMapper mapper,
                        RevalidacionIsrService revalidacion,
                        HorarioService horarioService) {
        this.lugarRepository = lugarRepository;
        this.traduccionRepository = traduccionRepository;
        this.horarioRepository = horarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.distritoRepository = distritoRepository;
        this.mapper = mapper;
        this.revalidacion = revalidacion;
        this.horarioService = horarioService;
    }

    /**
     * Listado público paginado (RF-01) con filtro por categoría (RF-04) y
     * búsqueda full-text (RF-02). Cada card incluye abiertoAhora (RF-09b).
     */
    @Transactional(readOnly = true)
    public Page<LugarResumenResponse> listar(String idioma, String categoriaCodigo,
                                             String q, Pageable pageable) {
        Page<Lugar> pagina;
        Map<UUID, CategoriaLugar> categorias = categoriaRepository.findAll().stream()
                .collect(Collectors.toMap(CategoriaLugar::getId, Function.identity()));

        if (q != null && !q.isBlank()) {
            pagina = lugarRepository.buscarPublicados(q.trim(), pageable);
        } else if (categoriaCodigo != null && !categoriaCodigo.isBlank()) {
            CategoriaLugar categoria = categoriaRepository.findByCodigo(categoriaCodigo)
                    .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND,
                            "CATEGORIA_NO_EXISTE", "La categoría indicada no existe."));
            pagina = lugarRepository.findByEstadoAndCategoriaLugarIdAndDeletedAtIsNull(
                    EstadoLugar.PUBLICADO, categoria.getId(), pageable);
        } else {
            pagina = lugarRepository.findByEstadoAndDeletedAtIsNull(EstadoLugar.PUBLICADO, pageable);
        }

        // Traducciones y estado abierto/cerrado de toda la página en una
        // consulta cada uno (evita N+1)
        List<UUID> ids = pagina.getContent().stream().map(Lugar::getId).toList();
        Map<UUID, LugarTraduccion> traducciones = traduccionRepository.findByLugarIdIn(ids).stream()
                .collect(Collectors.toMap(LugarTraduccion::getLugarId, Function.identity(),
                        // preferir el idioma solicitado; fallback al que llegue primero (es en seed)
                        (a, b) -> idioma.equals(a.getIdioma()) ? a : b));
        Map<UUID, Boolean> abiertos = horarioService.abiertosAhora(ids);

        return pagina.map(lugar -> mapper.aResumen(
                lugar,
                traducciones.get(lugar.getId()),
                categorias.get(lugar.getCategoriaLugarId()).getCodigo(),
                abiertos.get(lugar.getId())));
    }

    /** Ficha completa por slug (RF-09). */
    @Transactional(readOnly = true)
    public LugarDetalleResponse obtenerPorSlug(String slug) {
        Lugar lugar = lugarRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(this::lugarNoEncontrado);
        return detalleDe(lugar);
    }

    @Transactional
    public LugarDetalleResponse crear(LugarRequest request, UsuarioAutenticado admin) {
        if (lugarRepository.existsBySlug(request.slug())) {
            throw new NegocioException(HttpStatus.CONFLICT, "SLUG_YA_EXISTE",
                    "Ya existe un lugar con ese slug.");
        }
        validarReferencias(request);

        Lugar lugar = new Lugar();
        aplicarRequest(lugar, request);
        lugar.setCreatedBy(admin.id());
        lugar.setUpdatedBy(admin.id());
        lugarRepository.save(lugar);

        guardarTraduccionesYHorarios(lugar.getId(), request, admin);
        revalidacion.revalidarLugar(lugar.getSlug());
        return detalleDe(lugar);
    }

    @Transactional
    public LugarDetalleResponse actualizar(UUID id, LugarRequest request, UsuarioAutenticado admin) {
        Lugar lugar = lugarRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(this::lugarNoEncontrado);

        if (!lugar.getSlug().equals(request.slug()) && lugarRepository.existsBySlug(request.slug())) {
            throw new NegocioException(HttpStatus.CONFLICT, "SLUG_YA_EXISTE",
                    "Ya existe un lugar con ese slug.");
        }
        validarReferencias(request);

        aplicarRequest(lugar, request);
        lugar.setUpdatedBy(admin.id());
        lugarRepository.save(lugar);

        // Estrategia simple y consistente: reemplazo completo de hijas
        traduccionRepository.deleteByLugarId(id);
        horarioRepository.deleteByLugarId(id);
        guardarTraduccionesYHorarios(id, request, admin);

        revalidacion.revalidarLugar(lugar.getSlug());
        return detalleDe(lugar);
    }

    /** Soft delete: preserva integridad de reseñas/fotos históricas (10.3). */
    @Transactional
    public void eliminar(UUID id, UsuarioAutenticado admin) {
        Lugar lugar = lugarRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(this::lugarNoEncontrado);
        lugar.setDeletedAt(Instant.now());
        lugar.setUpdatedBy(admin.id());
        lugarRepository.save(lugar);
        revalidacion.revalidarLugar(lugar.getSlug());
    }

    // ------------------------------------------------------------------

    private void aplicarRequest(Lugar lugar, LugarRequest r) {
        lugar.setSlug(r.slug());
        lugar.setCategoriaLugarId(r.categoriaLugarId());
        lugar.setDistritoId(r.distritoId());
        lugar.setUbicacion(punto(r.longitud(), r.latitud()));
        lugar.setDireccion(r.direccion());
        lugar.setTelefono(r.telefono());
        lugar.setPrecioEntradaPen(r.precioEntradaPen());
        lugar.setDuracionVisitaMin(r.duracionVisitaMin());
        lugar.setAceptaTarjeta(r.aceptaTarjeta());
        lugar.setTieneBanos(r.tieneBanos());
        lugar.setAccesibleSillaRuedas(r.accesibleSillaRuedas());
        lugar.setAptoNinos(r.aptoNinos());
        lugar.setCostoTaxiDesdePlazaPen(r.costoTaxiDesdePlazaPen());
        lugar.setRequiereGuia(r.requiereGuia());
        lugar.setEstado(r.estado());
    }

    private void guardarTraduccionesYHorarios(UUID lugarId, LugarRequest request,
                                              UsuarioAutenticado admin) {
        List<LugarTraduccion> traducciones = request.traducciones().stream().map(t -> {
            LugarTraduccion traduccion = new LugarTraduccion();
            traduccion.setLugarId(lugarId);
            traduccion.setIdioma(t.idioma());
            traduccion.setNombre(t.nombre());
            traduccion.setDescripcion(t.descripcion());
            traduccion.setHistoria(t.historia());
            traduccion.setConsejos(t.consejos());
            traduccion.setUpdatedBy(admin.id());
            return traduccion;
        }).toList();
        traduccionRepository.saveAll(traducciones);

        if (request.horarios() != null) {
            List<HorarioLugar> horarios = request.horarios().stream().map(h -> {
                HorarioLugar horario = new HorarioLugar();
                horario.setLugarId(lugarId);
                horario.setDiaSemana(h.diaSemana());
                horario.setHoraApertura(h.cerrado() ? null : h.horaApertura());
                horario.setHoraCierre(h.cerrado() ? null : h.horaCierre());
                horario.setCerrado(h.cerrado());
                horario.setUpdatedBy(admin.id());
                return horario;
            }).toList();
            horarioRepository.saveAll(horarios);
        }
    }

    private void validarReferencias(LugarRequest request) {
        if (categoriaRepository.findById(request.categoriaLugarId()).isEmpty()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "CATEGORIA_NO_EXISTE",
                    "La categoría indicada no existe.");
        }
        if (!distritoRepository.existsById(request.distritoId())) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "DISTRITO_NO_EXISTE",
                    "El distrito indicado no existe.");
        }
    }

    private LugarDetalleResponse detalleDe(Lugar lugar) {
        String categoriaCodigo = categoriaRepository.findById(lugar.getCategoriaLugarId())
                .map(CategoriaLugar::getCodigo)
                .orElse(null);
        List<HorarioLugar> horarios =
                horarioRepository.findByLugarIdOrderByDiaSemanaAscHoraAperturaAsc(lugar.getId());
        Boolean abiertoAhora = horarios.isEmpty() ? null : horarioService.estaAbierto(horarios);
        return mapper.aDetalle(lugar, categoriaCodigo, abiertoAhora,
                traduccionRepository.findByLugarId(lugar.getId()),
                horarios);
    }

    private NegocioException lugarNoEncontrado() {
        return new NegocioException(HttpStatus.NOT_FOUND, "LUGAR_NO_ENCONTRADO",
                "El lugar solicitado no existe.");
    }

    private static Point punto(double longitud, double latitud) {
        return GEOMETRIAS.createPoint(new Coordinate(longitud, latitud));
    }
}
