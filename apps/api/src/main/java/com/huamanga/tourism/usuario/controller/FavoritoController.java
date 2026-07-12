package com.huamanga.tourism.usuario.controller;

import com.huamanga.tourism.common.security.UsuarioAutenticado;
import com.huamanga.tourism.lugar.dto.LugarResumenResponse;
import com.huamanga.tourism.usuario.service.FavoritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Favoritos del usuario (RF-35): siempre autenticado. */
@RestController
@SecurityRequirement(name = "bearer")
@Tag(name = "Favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @GetMapping("/favoritos")
    @Operation(summary = "Mis lugares favoritos como cards (RF-35)")
    public List<LugarResumenResponse> listar(
            @RequestParam(defaultValue = "es") String idioma,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return favoritoService.listar(usuario, idioma);
    }

    @GetMapping("/favoritos/slugs")
    @Operation(summary = "Slugs favoritos para pintar corazones en una sola llamada")
    public List<String> slugs(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return favoritoService.slugs(usuario);
    }

    @PutMapping("/lugares/{slug}/favorito")
    @Operation(summary = "Marcar favorito (idempotente)")
    public ResponseEntity<Void> marcar(
            @PathVariable String slug,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        favoritoService.marcar(slug, usuario);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/lugares/{slug}/favorito")
    @Operation(summary = "Quitar favorito")
    public ResponseEntity<Void> quitar(
            @PathVariable String slug,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        favoritoService.quitar(slug, usuario);
        return ResponseEntity.noContent().build();
    }
}
