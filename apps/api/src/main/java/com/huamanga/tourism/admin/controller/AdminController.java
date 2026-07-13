package com.huamanga.tourism.admin.controller;

import com.huamanga.tourism.admin.dto.MetricasResponse;
import com.huamanga.tourism.admin.service.MetricasService;
import com.huamanga.tourism.lugar.dto.LugarAdminResponse;
import com.huamanga.tourism.lugar.service.LugarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final LugarService lugarService;

    public AdminController(MetricasService metricasService, LugarService lugarService) {
        this.metricasService = metricasService;
        this.lugarService = lugarService;
    }

    @GetMapping("/metricas")
    @Operation(summary = "Resumen del dashboard: totales y colas de moderación (RF-52)")
    public MetricasResponse metricas() {
        return metricasService.resumen();
    }

    @GetMapping("/lugares")
    @Operation(summary = "Listado de gestión de lugares: incluye borradores y archivados (RF-47)")
    public Page<LugarAdminResponse> lugares(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "50") int tamano) {
        return lugarService.listarParaAdmin(
                PageRequest.of(Math.max(0, pagina), Math.min(Math.max(1, tamano), 100)));
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("pong", true, "modulo", "admin");
    }
}
