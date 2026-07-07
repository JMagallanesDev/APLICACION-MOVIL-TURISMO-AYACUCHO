package com.huamanga.tourism.reporte.service;

import com.huamanga.tourism.reporte.dominio.TipoIncidenteTraduccion;
import com.huamanga.tourism.reporte.dto.TipoIncidenteResponse;
import com.huamanga.tourism.reporte.repository.TipoIncidenteRepository;
import com.huamanga.tourism.reporte.repository.TipoIncidenteTraduccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Catálogo de tipos de incidente con nombre traducido (RF-70). */
@Service
public class TipoIncidenteService {

    private final TipoIncidenteRepository tipoRepository;
    private final TipoIncidenteTraduccionRepository traduccionRepository;

    public TipoIncidenteService(TipoIncidenteRepository tipoRepository,
                                TipoIncidenteTraduccionRepository traduccionRepository) {
        this.tipoRepository = tipoRepository;
        this.traduccionRepository = traduccionRepository;
    }

    @Transactional(readOnly = true)
    public List<TipoIncidenteResponse> listar(String idioma) {
        String lang = "en".equals(idioma) ? "en" : "es";
        Map<UUID, String> nombres = traduccionRepository.findByIdioma(lang).stream()
                .collect(Collectors.toMap(
                        TipoIncidenteTraduccion::getTipoIncidenteId,
                        TipoIncidenteTraduccion::getNombre));
        return tipoRepository.findAll().stream()
                .map(t -> new TipoIncidenteResponse(
                        t.getId(), t.getCodigo(),
                        nombres.getOrDefault(t.getId(), t.getCodigo()),
                        t.getIcono(), t.getColorHex()))
                .toList();
    }
}
