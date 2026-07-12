package com.huamanga.tourism.usuario.service;

import com.huamanga.tourism.common.exception.NegocioException;
import com.huamanga.tourism.common.security.UsuarioAutenticado;
import com.huamanga.tourism.lugar.dominio.Lugar;
import com.huamanga.tourism.lugar.dto.LugarResumenResponse;
import com.huamanga.tourism.lugar.repository.LugarRepository;
import com.huamanga.tourism.lugar.service.LugarService;
import com.huamanga.tourism.usuario.dominio.Favorito;
import com.huamanga.tourism.usuario.repository.FavoritoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Favoritos (RF-35): la PK compuesta (usuario, lugar) previene duplicados;
 * marcar es idempotente.
 */
@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final LugarRepository lugarRepository;
    private final LugarService lugarService;

    public FavoritoService(FavoritoRepository favoritoRepository,
                           LugarRepository lugarRepository,
                           LugarService lugarService) {
        this.favoritoRepository = favoritoRepository;
        this.lugarRepository = lugarRepository;
        this.lugarService = lugarService;
    }

    /** Cards de los lugares favoritos, en el orden en que se marcaron. */
    @Transactional(readOnly = true)
    public List<LugarResumenResponse> listar(UsuarioAutenticado usuario, String idioma) {
        List<Favorito> favoritos = favoritoRepository.findByUsuarioIdOrderByCreatedAtDesc(usuario.id());
        List<UUID> ids = favoritos.stream().map(Favorito::getLugarId).toList();
        List<Lugar> lugares = lugarRepository.findAllById(ids).stream()
                .filter(l -> l.getDeletedAt() == null)
                .toList();
        // preservar el orden de marcado
        Map<UUID, LugarResumenResponse> porId = lugarService.aResumenes(lugares, idioma).stream()
                .collect(Collectors.toMap(LugarResumenResponse::id, Function.identity()));
        return ids.stream().map(porId::get).filter(java.util.Objects::nonNull).toList();
    }

    /** Slugs favoritos para pintar corazones en el cliente en una llamada. */
    @Transactional(readOnly = true)
    public List<String> slugs(UsuarioAutenticado usuario) {
        List<UUID> ids = favoritoRepository.findByUsuarioIdOrderByCreatedAtDesc(usuario.id())
                .stream().map(Favorito::getLugarId).toList();
        return lugarRepository.findAllById(ids).stream()
                .filter(l -> l.getDeletedAt() == null)
                .map(Lugar::getSlug)
                .toList();
    }

    @Transactional
    public void marcar(String slug, UsuarioAutenticado usuario) {
        Lugar lugar = lugarPorSlug(slug);
        if (favoritoRepository.existsByUsuarioIdAndLugarId(usuario.id(), lugar.getId())) {
            return; // idempotente
        }
        Favorito favorito = new Favorito();
        favorito.setUsuarioId(usuario.id());
        favorito.setLugarId(lugar.getId());
        favoritoRepository.save(favorito);
    }

    @Transactional
    public void quitar(String slug, UsuarioAutenticado usuario) {
        Lugar lugar = lugarPorSlug(slug);
        favoritoRepository.eliminar(usuario.id(), lugar.getId());
    }

    private Lugar lugarPorSlug(String slug) {
        return lugarRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND,
                        "LUGAR_NO_ENCONTRADO", "El lugar solicitado no existe."));
    }
}
