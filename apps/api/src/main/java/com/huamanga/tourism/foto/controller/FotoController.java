package com.huamanga.tourism.foto.controller;

import com.huamanga.tourism.common.security.UsuarioAutenticado;
import com.huamanga.tourism.foto.dto.FotoResponse;
import com.huamanga.tourism.foto.service.FotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Galería de fotos de usuarios (RF-38): pública en lectura, autenticada en subida. */
@RestController
@Tag(name = "Fotos")
public class FotoController {

    private final FotoService fotoService;

    public FotoController(FotoService fotoService) {
        this.fotoService = fotoService;
    }

    @GetMapping("/lugares/{slug}/fotos")
    @Operation(summary = "Galería pública: solo fotos APROBADAS")
    public Page<FotoResponse> galeria(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "12") int tamano) {
        return fotoService.galeriaPublica(slug,
                PageRequest.of(Math.max(0, pagina), Math.min(Math.max(1, tamano), 50)));
    }

    @PostMapping(value = "/lugares/{slug}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Subir foto (máx 5 MB, JPG/PNG/WebP): queda PENDIENTE de moderación")
    public ResponseEntity<FotoResponse> subir(
            @PathVariable String slug,
            @RequestParam("archivo") MultipartFile archivo,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fotoService.subir(slug, archivo, usuario));
    }
}
