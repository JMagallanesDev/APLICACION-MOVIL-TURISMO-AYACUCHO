package com.huamanga.tourism.resena.service;

import com.huamanga.tourism.common.exception.NegocioException;
import com.huamanga.tourism.common.security.UsuarioAutenticado;
import com.huamanga.tourism.lugar.dominio.Lugar;
import com.huamanga.tourism.lugar.repository.LugarRepository;
import com.huamanga.tourism.resena.dominio.EstadoResena;
import com.huamanga.tourism.resena.dominio.Resena;
import com.huamanga.tourism.resena.dto.ResenaRequest;
import com.huamanga.tourism.resena.dto.ResenaResponse;
import com.huamanga.tourism.resena.repository.ResenaRepository;
import com.huamanga.tourism.usuario.dominio.Usuario;
import com.huamanga.tourism.usuario.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Reseñas (RF-37): una por usuario y lugar (UNIQUE en BD). El promedio nunca
 * se calcula aquí ni se guarda en Lugar: vive en la vista materializada.
 */
@Service
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final LugarRepository lugarRepository;
    private final UsuarioRepository usuarioRepository;

    public ResenaService(ResenaRepository resenaRepository,
                         LugarRepository lugarRepository,
                         UsuarioRepository usuarioRepository) {
        this.resenaRepository = resenaRepository;
        this.lugarRepository = lugarRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Page<ResenaResponse> listarPorLugar(String slug, Pageable pageable) {
        Lugar lugar = lugarPorSlug(slug);
        Page<Resena> pagina = resenaRepository
                .findByLugarIdAndEstadoOrderByCreatedAtDesc(lugar.getId(), EstadoResena.PUBLICADA, pageable);

        // Nombres de autores de la página en una sola consulta
        List<UUID> autores = pagina.getContent().stream().map(Resena::getUsuarioId).distinct().toList();
        Map<UUID, String> nombres = usuarioRepository.findAllById(autores).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNombre, (a, b) -> a));

        return pagina.map(resena -> new ResenaResponse(
                resena.getId(),
                resena.getCalificacion(),
                resena.getComentario(),
                nombres.getOrDefault(resena.getUsuarioId(), "Visitante"),
                resena.getCreatedAt()));
    }

    @Transactional
    public ResenaResponse crear(String slug, ResenaRequest request, UsuarioAutenticado usuario) {
        Lugar lugar = lugarPorSlug(slug);
        if (resenaRepository.existsByUsuarioIdAndLugarId(usuario.id(), lugar.getId())) {
            throw new NegocioException(HttpStatus.CONFLICT, "RESENA_YA_EXISTE",
                    "Ya publicaste una reseña para este lugar.");
        }
        Resena resena = new Resena();
        resena.setUsuarioId(usuario.id());
        resena.setLugarId(lugar.getId());
        resena.setCalificacion(request.calificacion());
        resena.setComentario(request.comentario() != null ? request.comentario().trim() : null);
        resena.setEstado(EstadoResena.PUBLICADA);
        resenaRepository.save(resena);

        return new ResenaResponse(resena.getId(), resena.getCalificacion(),
                resena.getComentario(), nombreDe(usuario.id()), resena.getCreatedAt());
    }

    /** El autor retira su reseña (o el ADMIN la elimina). */
    @Transactional
    public void eliminar(UUID resenaId, UsuarioAutenticado solicitante) {
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND,
                        "RESENA_NO_ENCONTRADA", "La reseña no existe."));

        boolean esPropia = resena.getUsuarioId().equals(solicitante.id());
        boolean esAdmin = "ADMIN".equals(solicitante.rol());
        if (!esPropia && !esAdmin) {
            throw new NegocioException(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                    "Solo puedes eliminar tus propias reseñas.");
        }
        resena.setEstado(EstadoResena.ELIMINADA);
        resenaRepository.save(resena);
    }

    /** Moderación (RF-50): el admin oculta o restaura reseñas. */
    @Transactional
    public void moderar(UUID resenaId, EstadoResena nuevoEstado) {
        if (nuevoEstado != EstadoResena.OCULTA && nuevoEstado != EstadoResena.PUBLICADA) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "ESTADO_INVALIDO",
                    "La moderación solo admite OCULTA o PUBLICADA.");
        }
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND,
                        "RESENA_NO_ENCONTRADA", "La reseña no existe."));
        resena.setEstado(nuevoEstado);
        resenaRepository.save(resena);
    }

    @Transactional(readOnly = true)
    public Page<ResenaResponse> bandejaModeracion(EstadoResena estado, Pageable pageable) {
        Page<Resena> pagina = resenaRepository.findByEstadoOrderByCreatedAtDesc(estado, pageable);
        List<UUID> autores = pagina.getContent().stream().map(Resena::getUsuarioId).distinct().toList();
        Map<UUID, String> nombres = usuarioRepository.findAllById(autores).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNombre, (a, b) -> a));
        return pagina.map(resena -> new ResenaResponse(
                resena.getId(), resena.getCalificacion(), resena.getComentario(),
                nombres.getOrDefault(resena.getUsuarioId(), "Visitante"), resena.getCreatedAt()));
    }

    // ------------------------------------------------------------------

    private Lugar lugarPorSlug(String slug) {
        return lugarRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND,
                        "LUGAR_NO_ENCONTRADO", "El lugar solicitado no existe."));
    }

    private String nombreDe(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId).map(Usuario::getNombre).orElse("Visitante");
    }
}
