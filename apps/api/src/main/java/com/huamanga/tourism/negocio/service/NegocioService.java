package com.huamanga.tourism.negocio.service;

import com.huamanga.tourism.common.exception.NegocioException;
import com.huamanga.tourism.common.security.UsuarioAutenticado;
import com.huamanga.tourism.negocio.dominio.CategoriaNegocio;
import com.huamanga.tourism.negocio.dominio.EstadoNegocio;
import com.huamanga.tourism.negocio.dominio.Negocio;
import com.huamanga.tourism.negocio.dominio.NegocioTraduccion;
import com.huamanga.tourism.negocio.dto.NegocioRequest;
import com.huamanga.tourism.negocio.dto.NegocioResponse;
import com.huamanga.tourism.negocio.repository.CategoriaNegocioRepository;
import com.huamanga.tourism.negocio.repository.NegocioRepository;
import com.huamanga.tourism.negocio.repository.NegocioTraduccionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Directorio de negocios (Módulo 13): el registro nace PENDIENTE hasta
 * aprobación del admin (RF-104); el público solo ve APROBADOS (RF-105).
 * El distrito del MVP es Huamanga capital (seed V5).
 */
@Service
public class NegocioService {

    private static final UUID DISTRITO_AYACUCHO =
            UUID.fromString("01980000-0000-7000-8000-000000000301");

    private final NegocioRepository negocioRepository;
    private final NegocioTraduccionRepository traduccionRepository;
    private final CategoriaNegocioRepository categoriaRepository;

    public NegocioService(NegocioRepository negocioRepository,
                          NegocioTraduccionRepository traduccionRepository,
                          CategoriaNegocioRepository categoriaRepository) {
        this.negocioRepository = negocioRepository;
        this.traduccionRepository = traduccionRepository;
        this.categoriaRepository = categoriaRepository;
    }

    /** Listado público (RF-105): aprobados, con descripción en el idioma pedido. */
    @Transactional(readOnly = true)
    public Page<NegocioResponse> listarAprobados(String idioma, UUID categoriaId, Pageable pageable) {
        Page<Negocio> pagina = categoriaId == null
                ? negocioRepository.findByEstadoAndDeletedAtIsNullOrderByNombreAsc(
                        EstadoNegocio.APROBADO, pageable)
                : negocioRepository.findByEstadoAndCategoriaNegocioIdAndDeletedAtIsNullOrderByNombreAsc(
                        EstadoNegocio.APROBADO, categoriaId, pageable);
        return mapear(pagina, idioma, false);
    }

    /** Registro del negocio propio (RF-104): nace PENDIENTE. */
    @Transactional
    public NegocioResponse registrar(NegocioRequest request, UsuarioAutenticado usuario) {
        if (negocioRepository.existsByUsuarioIdAndDeletedAtIsNull(usuario.id())) {
            throw new NegocioException(HttpStatus.CONFLICT, "NEGOCIO_YA_REGISTRADO",
                    "Ya registraste un negocio. Puedes editarlo desde tu panel.");
        }
        validarCategoria(request.categoriaNegocioId());

        Negocio negocio = new Negocio();
        negocio.setUsuarioId(usuario.id());
        aplicar(negocio, request);
        negocio.setEstado(EstadoNegocio.PENDIENTE);
        negocio.setCreatedBy(usuario.id());
        negocio.setUpdatedBy(usuario.id());
        negocioRepository.save(negocio);
        guardarTraducciones(negocio.getId(), request, usuario);

        return aResponse(negocio, codigoCategoria(negocio.getCategoriaNegocioId()),
                request.descripcionEs(), true);
    }

    /** Panel propio (RF-107). */
    @Transactional(readOnly = true)
    public NegocioResponse mio(UsuarioAutenticado usuario) {
        Negocio negocio = negocioRepository.findByUsuarioIdAndDeletedAtIsNull(usuario.id())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND,
                        "SIN_NEGOCIO", "Aún no registraste un negocio."));
        String descripcion = traduccionRepository.findByNegocioId(negocio.getId()).stream()
                .filter(t -> "es".equals(t.getIdioma()))
                .map(NegocioTraduccion::getDescripcion)
                .findFirst().orElse(null);
        return aResponse(negocio, codigoCategoria(negocio.getCategoriaNegocioId()), descripcion, true);
    }

    /** Edición del negocio propio: mantiene el estado (el admin puede suspender). */
    @Transactional
    public NegocioResponse actualizarMio(NegocioRequest request, UsuarioAutenticado usuario) {
        Negocio negocio = negocioRepository.findByUsuarioIdAndDeletedAtIsNull(usuario.id())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND,
                        "SIN_NEGOCIO", "Aún no registraste un negocio."));
        validarCategoria(request.categoriaNegocioId());
        aplicar(negocio, request);
        negocio.setUpdatedBy(usuario.id());
        negocioRepository.save(negocio);
        traduccionRepository.deleteByNegocioId(negocio.getId());
        guardarTraducciones(negocio.getId(), request, usuario);
        return aResponse(negocio, codigoCategoria(negocio.getCategoriaNegocioId()),
                request.descripcionEs(), true);
    }

    /** Moderación del admin: aprobar, rechazar o suspender. */
    @Transactional
    public void moderar(UUID negocioId, EstadoNegocio nuevoEstado) {
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND,
                        "NEGOCIO_NO_ENCONTRADO", "El negocio no existe."));
        negocio.setEstado(nuevoEstado);
        negocioRepository.save(negocio);
    }

    @Transactional(readOnly = true)
    public Page<NegocioResponse> bandeja(EstadoNegocio estado, Pageable pageable) {
        return mapear(negocioRepository.findByEstadoAndDeletedAtIsNullOrderByCreatedAtAsc(
                estado, pageable), "es", true);
    }

    // ------------------------------------------------------------------

    private Page<NegocioResponse> mapear(Page<Negocio> pagina, String idioma, boolean conEstado) {
        List<UUID> ids = pagina.getContent().stream().map(Negocio::getId).toList();
        Map<UUID, NegocioTraduccion> descripciones =
                traduccionRepository.findByNegocioIdIn(ids).stream()
                        .collect(Collectors.toMap(NegocioTraduccion::getNegocioId,
                                Function.identity(),
                                (a, b) -> idioma.equals(a.getIdioma()) ? a : b));
        Map<UUID, String> categorias = categoriaRepository.findAll().stream()
                .collect(Collectors.toMap(CategoriaNegocio::getId, CategoriaNegocio::getCodigo));

        return pagina.map(n -> {
            NegocioTraduccion t = descripciones.get(n.getId());
            return aResponse(n, categorias.get(n.getCategoriaNegocioId()),
                    t != null ? t.getDescripcion() : null, conEstado);
        });
    }

    private NegocioResponse aResponse(Negocio n, String categoriaCodigo,
                                      String descripcion, boolean conEstado) {
        return new NegocioResponse(n.getId(), n.getNombre(), categoriaCodigo, descripcion,
                n.getWhatsapp(), n.getTelefono(), n.getDireccion(), n.getHorario(),
                conEstado ? n.getEstado() : null);
    }

    private void aplicar(Negocio negocio, NegocioRequest r) {
        negocio.setCategoriaNegocioId(r.categoriaNegocioId());
        negocio.setDistritoId(DISTRITO_AYACUCHO);
        negocio.setNombre(r.nombre().trim());
        negocio.setRuc(r.ruc());
        negocio.setTelefono(r.telefono());
        negocio.setWhatsapp(r.whatsapp().replaceAll("\\D", ""));
        negocio.setDireccion(r.direccion());
        negocio.setHorario(r.horario());
    }

    private void guardarTraducciones(UUID negocioId, NegocioRequest r, UsuarioAutenticado usuario) {
        NegocioTraduccion es = new NegocioTraduccion();
        es.setNegocioId(negocioId);
        es.setIdioma("es");
        es.setDescripcion(r.descripcionEs().trim());
        es.setUpdatedBy(usuario.id());
        traduccionRepository.save(es);

        if (r.descripcionEn() != null && !r.descripcionEn().isBlank()) {
            NegocioTraduccion en = new NegocioTraduccion();
            en.setNegocioId(negocioId);
            en.setIdioma("en");
            en.setDescripcion(r.descripcionEn().trim());
            en.setUpdatedBy(usuario.id());
            traduccionRepository.save(en);
        }
    }

    private void validarCategoria(UUID categoriaId) {
        if (!categoriaRepository.existsById(categoriaId)) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "CATEGORIA_NO_EXISTE",
                    "La categoría indicada no existe.");
        }
    }

    private String codigoCategoria(UUID id) {
        return categoriaRepository.findById(id).map(CategoriaNegocio::getCodigo).orElse(null);
    }
}
