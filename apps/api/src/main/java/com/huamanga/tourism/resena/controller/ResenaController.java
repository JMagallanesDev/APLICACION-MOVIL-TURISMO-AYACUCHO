package com.huamanga.tourism.resena.controller;

import com.huamanga.tourism.common.security.UsuarioAutenticado;
import com.huamanga.tourism.resena.dto.ResenaRequest;
import com.huamanga.tourism.resena.dto.ResenaResponse;
import com.huamanga.tourism.resena.service.ResenaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Reseñas (RF-37): lectura pública, escritura de usuarios registrados. */
@RestController
@Tag(name = "Reseñas")
public class ResenaController {

    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    @GetMapping("/lugares/{slug}/resenas")
    @Operation(summary = "Reseñas publicadas de un lugar (público)")
    public Page<ResenaResponse> listar(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {
        return resenaService.listarPorLugar(slug,
                PageRequest.of(Math.max(0, pagina), Math.min(Math.max(1, tamano), 50)));
    }

    @PostMapping("/lugares/{slug}/resenas")
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Publicar reseña: una por usuario y lugar (RF-37)")
    public ResponseEntity<ResenaResponse> crear(
            @PathVariable String slug,
            @Valid @RequestBody ResenaRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resenaService.crear(slug, request, usuario));
    }

    @DeleteMapping("/resenas/{id}")
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Retirar reseña propia (o cualquier reseña siendo ADMIN)")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        resenaService.eliminar(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
