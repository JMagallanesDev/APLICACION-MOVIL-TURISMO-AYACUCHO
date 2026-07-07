package com.huamanga.tourism.reporte.controller;

import com.huamanga.tourism.common.security.ClienteIp;
import com.huamanga.tourism.common.security.UsuarioAutenticado;
import com.huamanga.tourism.reporte.dto.IncidenteMapaResponse;
import com.huamanga.tourism.reporte.dto.ReporteRequest;
import com.huamanga.tourism.reporte.dto.ReporteResponse;
import com.huamanga.tourism.reporte.dto.TipoIncidenteResponse;
import com.huamanga.tourism.reporte.service.ReporteService;
import com.huamanga.tourism.reporte.service.TipoIncidenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Preservación ciudadana (diferenciador): todo público, sin cuenta requerida
 * (RF-69/71/72/74). El reporte se crea vía multipart: datos JSON + hasta 5
 * fotos opcionales.
 */
@RestController
@Tag(name = "Preservación ciudadana")
public class ReporteController {

    private final ReporteService reporteService;
    private final TipoIncidenteService tipoIncidenteService;

    public ReporteController(ReporteService reporteService,
                             TipoIncidenteService tipoIncidenteService) {
        this.reporteService = reporteService;
        this.tipoIncidenteService = tipoIncidenteService;
    }

    @GetMapping("/tipos-incidente")
    @Operation(summary = "7 tipos de incidente predefinidos, traducidos (RF-70)")
    public List<TipoIncidenteResponse> tipos(@RequestParam(defaultValue = "es") String idioma) {
        return tipoIncidenteService.listar(idioma);
    }

    @PostMapping(value = "/reportes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear reporte anónimo o identificado con hasta 5 fotos (RF-69/71/72)")
    public ResponseEntity<ReporteResponse> crear(
            @Valid @RequestPart("datos") ReporteRequest datos,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos,
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            HttpServletRequest request) {
        // La IP solo se usa para el contador anti-spam en Redis; nunca se persiste
        String ipEfimera = ClienteIp.resolver(request);
        ReporteResponse creado = reporteService.crear(
                datos, fotos, usuario != null ? usuario.id() : null, ipEfimera);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/reportes/mapa")
    @Operation(summary = "Mapa público: solo incidentes aprobados o resueltos (RF-74)")
    public List<IncidenteMapaResponse> mapa() {
        return reporteService.mapaPublico();
    }
}
