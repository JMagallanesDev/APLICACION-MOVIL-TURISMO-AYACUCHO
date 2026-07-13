package com.huamanga.tourism.lugar.controller;

import com.huamanga.tourism.lugar.repository.CategoriaLugarRepository;
import com.huamanga.tourism.lugar.repository.CategoriaLugarTraduccionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Catálogo de categorías de lugar traducido (RF-04). Controller propio:
 * si viviera bajo /lugares chocaría con el matcher /lugares/{slug}.
 */
@RestController
@Tag(name = "Lugares")
public class CategoriaLugarController {

    public record CategoriaLugarResponse(UUID id, String codigo, String nombre,
                                         String icono, String colorHex) {
    }

    private final CategoriaLugarRepository categoriaRepository;
    private final CategoriaLugarTraduccionRepository traduccionRepository;

    public CategoriaLugarController(CategoriaLugarRepository categoriaRepository,
                                    CategoriaLugarTraduccionRepository traduccionRepository) {
        this.categoriaRepository = categoriaRepository;
        this.traduccionRepository = traduccionRepository;
    }

    @GetMapping("/categorias-lugar")
    @Operation(summary = "8 categorías de lugar, traducidas")
    public List<CategoriaLugarResponse> listar(@RequestParam(defaultValue = "es") String idioma) {
        String lang = "en".equals(idioma) ? "en" : "es";
        Map<UUID, String> nombres = traduccionRepository.findByIdioma(lang).stream()
                .collect(Collectors.toMap(
                        t -> t.getCategoriaLugarId(), t -> t.getNombre()));
        return categoriaRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(a.getOrden(), b.getOrden()))
                .map(c -> new CategoriaLugarResponse(c.getId(), c.getCodigo(),
                        nombres.getOrDefault(c.getId(), c.getCodigo()), c.getIcono(), c.getColorHex()))
                .toList();
    }
}
