package com.huamanga.tourism.reporte.repository;

import com.huamanga.tourism.reporte.dominio.EstadoReporte;
import com.huamanga.tourism.reporte.dominio.Reporte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, UUID> {

    /** Mapa público (RF-74): solo incidentes aprobados o resueltos. */
    List<Reporte> findByEstadoIn(Collection<EstadoReporte> estados);

    /** Bandeja de moderación (RF-76). */
    Page<Reporte> findByEstadoOrderByCreatedAtAsc(EstadoReporte estado, Pageable pageable);
}
