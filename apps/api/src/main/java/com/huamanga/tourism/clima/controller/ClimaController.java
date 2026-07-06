package com.huamanga.tourism.clima.controller;

import com.huamanga.tourism.clima.dto.ClimaResponse;
import com.huamanga.tourism.clima.service.ClimaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clima")
@Tag(name = "Clima", description = "Clima actual sobre Huamanga (RF-25)")
public class ClimaController {

    private final ClimaService climaService;

    public ClimaController(ClimaService climaService) {
        this.climaService = climaService;
    }

    @GetMapping("/actual")
    @Operation(summary = "Clima actual con caché de 30 min y fallback al último dato (RF-25)")
    public ClimaResponse actual(@RequestParam(defaultValue = "es") String idioma) {
        return climaService.actual("en".equals(idioma) ? "en" : "es");
    }
}
