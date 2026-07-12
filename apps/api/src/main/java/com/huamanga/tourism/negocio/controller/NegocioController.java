package com.huamanga.tourism.negocio.controller;

import com.huamanga.tourism.common.security.UsuarioAutenticado;
import com.huamanga.tourism.negocio.dominio.CategoriaNegocio;
import com.huamanga.tourism.negocio.dto.NegocioRequest;
import com.huamanga.tourism.negocio.dto.NegocioResponse;
import com.huamanga.tourism.negocio.repository.CategoriaNegocioRepository;
import com.huamanga.tourism.negocio.repository.CategoriaNegocioTraduccionRepository;
import com.huamanga.tourism.negocio.service.NegocioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Directorio de negocios (Módulo 13): listado público, registro y panel propio. */
@RestController
@Tag(name = "Directorio de negocios")
public class NegocioController {

    private final NegocioService negocioService;
    private final CategoriaNegocioRepository categoriaRepository;
    private final CategoriaNegocioTraduccionRepository categoriaTraduccionRepository;

    public NegocioController(NegocioService negocioService,
                             CategoriaNegocioRepository categoriaRepository,
                             CategoriaNegocioTraduccionRepository categoriaTraduccionRepository) {
        this.negocioService = negocioService;
        this.categoriaRepository = categoriaRepository;
        this.categoriaTraduccionRepository = categoriaTraduccionRepository;
    }

    public record CategoriaNegocioResponse(UUID id, String codigo, String nombre, String icono) {
    }

    @GetMapping("/categorias-negocio")
    @Operation(summary = "Categorías del directorio, traducidas")
    public List<CategoriaNegocioResponse> categorias(
            @RequestParam(defaultValue = "es") String idioma) {
        String lang = "en".equals(idioma) ? "en" : "es";
        Map<UUID, String> nombres = categoriaTraduccionRepository.findByIdioma(lang).stream()
                .collect(Collectors.toMap(
                        t -> t.getCategoriaNegocioId(), t -> t.getNombre()));
        return categoriaRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(a.getOrden(), b.getOrden()))
                .map(c -> new CategoriaNegocioResponse(c.getId(), c.getCodigo(),
                        nombres.getOrDefault(c.getId(), c.getCodigo()), c.getIcono()))
                .toList();
    }

    @GetMapping("/negocios")
    @Operation(summary = "Listado público de negocios aprobados (RF-105)")
    public Page<NegocioResponse> listar(
            @RequestParam(defaultValue = "es") String idioma,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "12") int tamano) {
        return negocioService.listarAprobados(idioma, categoriaId,
                PageRequest.of(Math.max(0, pagina), Math.min(Math.max(1, tamano), 50)));
    }

    @PostMapping("/negocios")
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Registrar mi negocio: queda PENDIENTE hasta aprobación (RF-104)")
    public ResponseEntity<NegocioResponse> registrar(
            @Valid @RequestBody NegocioRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(negocioService.registrar(request, usuario));
    }

    @GetMapping("/negocios/mio")
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Mi negocio con su estado (RF-107)")
    public NegocioResponse mio(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return negocioService.mio(usuario);
    }

    @PutMapping("/negocios/mio")
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Editar el perfil de mi negocio (RF-107)")
    public NegocioResponse actualizarMio(
            @Valid @RequestBody NegocioRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return negocioService.actualizarMio(request, usuario);
    }
}
