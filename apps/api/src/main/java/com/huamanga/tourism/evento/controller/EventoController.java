package com.huamanga.tourism.evento.controller;

import com.huamanga.tourism.common.security.UsuarioAutenticado;
import com.huamanga.tourism.evento.dto.EventoDetalleResponse;
import com.huamanga.tourism.evento.dto.EventoRequest;
import com.huamanga.tourism.evento.dto.EventoResumenResponse;
import com.huamanga.tourism.evento.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Agenda cultural: lectura pública (RF-79/80/84/84b), escritura solo ADMIN (RF-86). */
@RestController
@RequestMapping("/eventos")
@Tag(name = "Agenda cultural")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping("/proximos")
    @Operation(summary = "Próximos eventos publicados (RF-84)")
    public List<EventoResumenResponse> proximos(
            @RequestParam(defaultValue = "es") String idioma,
            @RequestParam(defaultValue = "6") int limite) {
        return eventoService.proximos(idioma, Math.min(Math.max(1, limite), 30));
    }

    @GetMapping
    @Operation(summary = "Eventos en un rango de fechas: calendario y 'Durante mi visita' (RF-79/84b)")
    public List<EventoResumenResponse> enRango(
            @RequestParam(defaultValue = "es") String idioma,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return eventoService.enRango(idioma, desde, hasta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ficha del evento (RF-80)")
    public EventoDetalleResponse detalle(@PathVariable UUID id) {
        return eventoService.detalle(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Crear evento (RF-86, solo ADMIN)")
    public ResponseEntity<EventoDetalleResponse> crear(
            @Valid @RequestBody EventoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado admin) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.crear(request, admin));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Actualizar evento (solo ADMIN)")
    public EventoDetalleResponse actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody EventoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado admin) {
        return eventoService.actualizar(id, request, admin);
    }

    @PostMapping("/{id}/clonar")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Clonar evento sumando N años a sus fechas (RF-86)")
    public ResponseEntity<EventoDetalleResponse> clonar(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int anios,
            @AuthenticationPrincipal UsuarioAutenticado admin) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventoService.clonarAnual(id, Math.min(Math.max(1, anios), 10), admin));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Eliminar evento (soft delete, solo ADMIN)")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UsuarioAutenticado admin) {
        eventoService.eliminar(id, admin);
        return ResponseEntity.noContent().build();
    }
}
