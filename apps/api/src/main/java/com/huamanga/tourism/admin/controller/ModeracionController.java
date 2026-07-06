package com.huamanga.tourism.admin.controller;

import com.huamanga.tourism.foto.dominio.EstadoFoto;
import com.huamanga.tourism.foto.dto.FotoResponse;
import com.huamanga.tourism.foto.service.FotoService;
import com.huamanga.tourism.resena.dominio.EstadoResena;
import com.huamanga.tourism.resena.dto.ResenaResponse;
import com.huamanga.tourism.resena.service.ResenaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Bandejas de moderación del admin (RF-49 fotos, RF-50 reseñas). */
@RestController
@RequestMapping("/admin/moderacion")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer")
@Tag(name = "Moderación (admin)")
public class ModeracionController {

    public record ModeracionFotoRequest(EstadoFoto estado, String motivoRechazo) {
    }

    public record ModeracionResenaRequest(EstadoResena estado) {
    }

    private final FotoService fotoService;
    private final ResenaService resenaService;

    public ModeracionController(FotoService fotoService, ResenaService resenaService) {
        this.fotoService = fotoService;
        this.resenaService = resenaService;
    }

    @GetMapping("/fotos")
    @Operation(summary = "Bandeja de fotos por estado (default PENDIENTE, RF-49)")
    public Page<FotoResponse> fotos(
            @RequestParam(defaultValue = "PENDIENTE") EstadoFoto estado,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        return fotoService.bandejaModeracion(estado,
                PageRequest.of(Math.max(0, pagina), Math.min(Math.max(1, tamano), 50)));
    }

    @PatchMapping("/fotos/{id}")
    @Operation(summary = "Aprobar o rechazar una foto (con motivo)")
    public FotoResponse moderarFoto(@PathVariable UUID id,
                                    @RequestBody ModeracionFotoRequest request) {
        return fotoService.moderar(id, request.estado(), request.motivoRechazo());
    }

    @GetMapping("/resenas")
    @Operation(summary = "Bandeja de reseñas por estado (RF-50)")
    public Page<ResenaResponse> resenas(
            @RequestParam(defaultValue = "EN_REVISION") EstadoResena estado,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        return resenaService.bandejaModeracion(estado,
                PageRequest.of(Math.max(0, pagina), Math.min(Math.max(1, tamano), 50)));
    }

    @PatchMapping("/resenas/{id}")
    @Operation(summary = "Ocultar o restaurar una reseña")
    public ResponseEntity<Void> moderarResena(@PathVariable UUID id,
                                              @RequestBody ModeracionResenaRequest request) {
        resenaService.moderar(id, request.estado());
        return ResponseEntity.noContent().build();
    }
}
