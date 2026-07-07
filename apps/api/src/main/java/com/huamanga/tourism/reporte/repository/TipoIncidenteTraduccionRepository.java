package com.huamanga.tourism.reporte.repository;

import com.huamanga.tourism.reporte.dominio.TipoIncidenteTraduccion;
import com.huamanga.tourism.reporte.dominio.TipoIncidenteTraduccionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoIncidenteTraduccionRepository
        extends JpaRepository<TipoIncidenteTraduccion, TipoIncidenteTraduccionId> {

    List<TipoIncidenteTraduccion> findByIdioma(String idioma);
}
