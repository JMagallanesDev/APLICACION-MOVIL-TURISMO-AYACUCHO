package com.huamanga.tourism.usuario.controller;

import com.huamanga.tourism.common.security.UsuarioAutenticado;
import com.huamanga.tourism.usuario.service.ReporteContenidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Reporte de contenido inapropiado (RF-45): usuarios registrados. */
@RestController
@SecurityRequirement(name = "bearer")
@Tag(name = "Reporte de contenido")
public class ReporteContenidoController {

    public record ReporteContenidoRequest(
            UUID fotoId,
            UUID resenaId,
            @NotBlank(message = "indica el motivo") @Size(max = 300) String motivo) {
    }

    private final ReporteContenidoService service;

    public ReporteContenidoController(ReporteContenidoService service) {
        this.service = service;
    }

    @PostMapping("/reportes-contenido")
    @Operation(summary = "Reportar una foto o reseña; al 3er reporte pasa a revisión (RF-45)")
    public ResponseEntity<Void> reportar(
            @Valid @RequestBody ReporteContenidoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        service.reportar(request.fotoId(), request.resenaId(), request.motivo(), usuario);
        return ResponseEntity.noContent().build();
    }
}
