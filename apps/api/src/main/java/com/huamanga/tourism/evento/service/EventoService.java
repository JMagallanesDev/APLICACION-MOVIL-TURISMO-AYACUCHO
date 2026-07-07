package com.huamanga.tourism.evento.service;

import com.huamanga.tourism.common.exception.NegocioException;
import com.huamanga.tourism.common.security.UsuarioAutenticado;
import com.huamanga.tourism.evento.dominio.EstadoEvento;
import com.huamanga.tourism.evento.dominio.Evento;
import com.huamanga.tourism.evento.dominio.EventoTraduccion;
import com.huamanga.tourism.evento.dto.EventoDetalleResponse;
import com.huamanga.tourism.evento.dto.EventoRequest;
import com.huamanga.tourism.evento.dto.EventoResumenResponse;
import com.huamanga.tourism.evento.dto.TraduccionEventoDto;
import com.huamanga.tourism.evento.repository.EventoRepository;
import com.huamanga.tourism.evento.repository.EventoTraduccionRepository;
import com.huamanga.tourism.geografia.repository.DistritoRepository;
import com.huamanga.tourism.lugar.repository.LugarRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Agenda cultural (Módulo 9). Contenido gestionado por el admin y persistido
 * en BD; nombres resueltos al idioma pedido con fallback a español.
 */
@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final EventoTraduccionRepository traduccionRepository;
    private final DistritoRepository distritoRepository;
    private final LugarRepository lugarRepository;

    public EventoService(EventoRepository eventoRepository,
                         EventoTraduccionRepository traduccionRepository,
                         DistritoRepository distritoRepository,
                         LugarRepository lugarRepository) {
        this.eventoRepository = eventoRepository;
        this.traduccionRepository = traduccionRepository;
        this.distritoRepository = distritoRepository;
        this.lugarRepository = lugarRepository;
    }

    /** Próximos eventos publicados (RF-84). */
    @Transactional(readOnly = true)
    public List<EventoResumenResponse> proximos(String idioma, int limite) {
        List<Evento> eventos = eventoRepository.proximos(
                EstadoEvento.PUBLICADO, LocalDate.now(), PageRequest.of(0, limite));
        return aResumen(eventos, idioma);
    }

    /**
     * Eventos que coinciden con un rango (RF-79 calendario por mes y RF-84b
     * "Durante mi visita").
     */
    @Transactional(readOnly = true)
    public List<EventoResumenResponse> enRango(String idioma, LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "RANGO_INVALIDO",
                    "La fecha final no puede ser anterior a la inicial.");
        }
        List<Evento> eventos = eventoRepository.enRango(EstadoEvento.PUBLICADO, desde, hasta);
        return aResumen(eventos, idioma);
    }

    @Transactional(readOnly = true)
    public EventoDetalleResponse detalle(UUID id) {
        Evento evento = eventoRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(this::noEncontrado);
        return aDetalle(evento);
    }

    @Transactional
    public EventoDetalleResponse crear(EventoRequest request, UsuarioAutenticado admin) {
        validarReferencias(request);
        Evento evento = new Evento();
        aplicar(evento, request);
        evento.setCreatedBy(admin.id());
        evento.setUpdatedBy(admin.id());
        eventoRepository.save(evento);
        guardarTraducciones(evento.getId(), request.traducciones(), admin);
        return aDetalle(evento);
    }

    @Transactional
    public EventoDetalleResponse actualizar(UUID id, EventoRequest request, UsuarioAutenticado admin) {
        Evento evento = eventoRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(this::noEncontrado);
        validarReferencias(request);
        aplicar(evento, request);
        evento.setUpdatedBy(admin.id());
        eventoRepository.save(evento);
        traduccionRepository.deleteByEventoId(id);
        guardarTraducciones(id, request.traducciones(), admin);
        return aDetalle(evento);
    }

    @Transactional
    public void eliminar(UUID id, UsuarioAutenticado admin) {
        Evento evento = eventoRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(this::noEncontrado);
        evento.setDeletedAt(java.time.Instant.now());
        evento.setUpdatedBy(admin.id());
        eventoRepository.save(evento);
    }

    /** Clonado anual (RF-86): duplica un evento sumando N años a sus fechas. */
    @Transactional
    public EventoDetalleResponse clonarAnual(UUID id, int anios, UsuarioAutenticado admin) {
        Evento original = eventoRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(this::noEncontrado);
        Evento copia = new Evento();
        copia.setTipo(original.getTipo());
        copia.setDistritoId(original.getDistritoId());
        copia.setLugarId(original.getLugarId());
        copia.setCloudinaryUrlPortada(original.getCloudinaryUrlPortada());
        copia.setFechaInicio(original.getFechaInicio().plusYears(anios));
        copia.setFechaFin(original.getFechaFin().plusYears(anios));
        copia.setRecurrenteAnual(original.isRecurrenteAnual());
        copia.setEstado(EstadoEvento.BORRADOR); // la copia nace en borrador para revisar
        copia.setCreatedBy(admin.id());
        copia.setUpdatedBy(admin.id());
        eventoRepository.save(copia);

        List<TraduccionEventoDto> traducciones = traduccionRepository.findByEventoId(id).stream()
                .map(t -> new TraduccionEventoDto(t.getIdioma(), t.getNombre(),
                        t.getDescripcion(), t.getOrganizador()))
                .toList();
        guardarTraducciones(copia.getId(), traducciones, admin);
        return aDetalle(copia);
    }

    // ------------------------------------------------------------------

    private List<EventoResumenResponse> aResumen(List<Evento> eventos, String idioma) {
        List<UUID> ids = eventos.stream().map(Evento::getId).toList();
        Map<UUID, EventoTraduccion> traducciones = traduccionRepository.findByEventoIdIn(ids).stream()
                .collect(Collectors.toMap(EventoTraduccion::getEventoId, Function.identity(),
                        (a, b) -> idioma.equals(a.getIdioma()) ? a : b));
        return eventos.stream().map(e -> {
            EventoTraduccion t = traducciones.get(e.getId());
            return new EventoResumenResponse(e.getId(), e.getTipo(),
                    t != null ? t.getNombre() : "", e.getFechaInicio(), e.getFechaFin(),
                    e.getCloudinaryUrlPortada());
        }).toList();
    }

    private EventoDetalleResponse aDetalle(Evento e) {
        String lugarSlug = e.getLugarId() == null ? null
                : lugarRepository.findById(e.getLugarId()).map(l -> l.getSlug()).orElse(null);
        List<TraduccionEventoDto> traducciones = traduccionRepository.findByEventoId(e.getId()).stream()
                .map(t -> new TraduccionEventoDto(t.getIdioma(), t.getNombre(),
                        t.getDescripcion(), t.getOrganizador()))
                .toList();
        return new EventoDetalleResponse(e.getId(), e.getTipo(), e.getEstado(),
                e.getDistritoId(), e.getLugarId(), lugarSlug, e.getFechaInicio(), e.getFechaFin(),
                e.isRecurrenteAnual(), e.getCloudinaryUrlPortada(), traducciones);
    }

    private void aplicar(Evento evento, EventoRequest r) {
        evento.setTipo(r.tipo());
        evento.setDistritoId(r.distritoId());
        evento.setLugarId(r.lugarId());
        evento.setCloudinaryUrlPortada(r.cloudinaryUrlPortada());
        evento.setFechaInicio(r.fechaInicio());
        evento.setFechaFin(r.fechaFin());
        evento.setRecurrenteAnual(r.recurrenteAnual());
        evento.setEstado(r.estado());
    }

    private void guardarTraducciones(UUID eventoId, List<TraduccionEventoDto> traducciones,
                                     UsuarioAutenticado admin) {
        List<EventoTraduccion> entidades = traducciones.stream().map(t -> {
            EventoTraduccion et = new EventoTraduccion();
            et.setEventoId(eventoId);
            et.setIdioma(t.idioma());
            et.setNombre(t.nombre());
            et.setDescripcion(t.descripcion());
            et.setOrganizador(t.organizador());
            et.setUpdatedBy(admin.id());
            return et;
        }).toList();
        traduccionRepository.saveAll(entidades);
    }

    private void validarReferencias(EventoRequest request) {
        if (!distritoRepository.existsById(request.distritoId())) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "DISTRITO_NO_EXISTE",
                    "El distrito indicado no existe.");
        }
        if (request.lugarId() != null
                && lugarRepository.findByIdAndDeletedAtIsNull(request.lugarId()).isEmpty()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "LUGAR_NO_EXISTE",
                    "El lugar indicado no existe.");
        }
    }

    private NegocioException noEncontrado() {
        return new NegocioException(HttpStatus.NOT_FOUND, "EVENTO_NO_ENCONTRADO",
                "El evento solicitado no existe.");
    }
}
