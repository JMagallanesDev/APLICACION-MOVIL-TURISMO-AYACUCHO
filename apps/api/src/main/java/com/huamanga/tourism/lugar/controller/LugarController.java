package com.huamanga.tourism.lugar.controller;

import com.huamanga.tourism.common.security.UsuarioAutenticado;
import com.huamanga.tourism.lugar.dto.LugarDetalleResponse;
import com.huamanga.tourism.lugar.dto.LugarRequest;
import com.huamanga.tourism.lugar.dto.LugarResumenResponse;
import com.huamanga.tourism.lugar.service.LugarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Catálogo de lugares: lectura pública (RF-34), escritura solo ADMIN (RF-47).
 */
@RestController
@RequestMapping("/lugares")
@Tag(name = "Lugares", description = "Catálogo de lugares patrimoniales")
public class LugarController {

    private static final int TAMANO_MAXIMO = 50;

    private final LugarService lugarService;

    public LugarController(LugarService lugarService) {
        this.lugarService = lugarService;
    }

    @GetMapping
    @Operation(summary = "Listado público paginado, filtrable por categoría (RF-01, RF-04)")
    public Page<LugarResumenResponse> listar(
            @RequestParam(defaultValue = "es") String idioma,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "12") int tamano) {
        PageRequest pageable = PageRequest.of(
                Math.max(0, pagina),
                Math.min(Math.max(1, tamano), TAMANO_MAXIMO),
                Sort.by("slug"));
        return lugarService.listar(idioma, categoria, pageable);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Ficha completa por slug (RF-09)")
    public LugarDetalleResponse detalle(@PathVariable String slug) {
        return lugarService.obtenerPorSlug(slug);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Crear lugar con traducciones y horarios (RF-47, solo ADMIN)")
    public ResponseEntity<LugarDetalleResponse> crear(
            @Valid @RequestBody LugarRequest request,
            @AuthenticationPrincipal UsuarioAutenticado admin) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lugarService.crear(request, admin));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Actualizar lugar (reemplaza traducciones y horarios)")
    public LugarDetalleResponse actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody LugarRequest request,
            @AuthenticationPrincipal UsuarioAutenticado admin) {
        return lugarService.actualizar(id, request, admin);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Eliminación lógica (soft delete)")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UsuarioAutenticado admin) {
        lugarService.eliminar(id, admin);
        return ResponseEntity.noContent().build();
    }
}
