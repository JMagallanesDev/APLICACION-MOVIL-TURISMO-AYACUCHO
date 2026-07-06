package com.huamanga.tourism.recomendacion.controller;

import com.huamanga.tourism.recomendacion.dto.RecomendacionResponse;
import com.huamanga.tourism.recomendacion.service.RecomendacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recomendaciones")
@Tag(name = "Recomendaciones", description = "Recomendación contextual '¿Qué hago ahora?' (RF-08)")
public class RecomendacionController {

    private final RecomendacionService recomendacionService;

    public RecomendacionController(RecomendacionService recomendacionService) {
        this.recomendacionService = recomendacionService;
    }

    @GetMapping("/ahora")
    @Operation(summary = "Lugares sugeridos según hora + clima + abierto/cerrado (RF-08)")
    public RecomendacionResponse ahora(
            @RequestParam(defaultValue = "es") String idioma,
            @RequestParam(defaultValue = "4") int limite) {
        return recomendacionService.ahora(idioma, Math.min(Math.max(1, limite), 10));
    }
}
