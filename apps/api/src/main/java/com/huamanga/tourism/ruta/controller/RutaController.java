package com.huamanga.tourism.ruta.controller;

import com.huamanga.tourism.ruta.dto.RutaResponse;
import com.huamanga.tourism.ruta.service.RutaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Rutas temáticas públicas (RF-53): lectura anónima, contenido por seed. */
@RestController
@RequestMapping("/rutas")
@Tag(name = "Rutas", description = "Rutas temáticas para recorrer Huamanga")
public class RutaController {

    private final RutaService rutaService;

    public RutaController(RutaService rutaService) {
        this.rutaService = rutaService;
    }

    @GetMapping
    @Operation(summary = "Rutas temáticas activas con sus paradas en orden (RF-53)")
    public List<RutaResponse> listar(@RequestParam(defaultValue = "es") String idioma) {
        return rutaService.listar(idioma);
    }
}
