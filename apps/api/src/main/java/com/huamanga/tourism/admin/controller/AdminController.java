package com.huamanga.tourism.admin.controller;

import com.huamanga.tourism.admin.dto.MetricasResponse;
import com.huamanga.tourism.admin.service.MetricasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Panel de administración (RF-52); autorización por rol en cada endpoint (RNF-16). */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer")
@Tag(name = "Administración")
public class AdminController {

    private final MetricasService metricasService;

    public AdminController(MetricasService metricasService) {
        this.metricasService = metricasService;
    }

    @GetMapping("/metricas")
    @Operation(summary = "Resumen del dashboard: totales y colas de moderación (RF-52)")
    public MetricasResponse metricas() {
        return metricasService.resumen();
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("pong", true, "modulo", "admin");
    }
}
