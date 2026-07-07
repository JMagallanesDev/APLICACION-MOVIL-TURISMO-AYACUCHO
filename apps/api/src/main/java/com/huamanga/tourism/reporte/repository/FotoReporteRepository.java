package com.huamanga.tourism.reporte.repository;

import com.huamanga.tourism.reporte.dominio.FotoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FotoReporteRepository extends JpaRepository<FotoReporte, UUID> {

    List<FotoReporte> findByReporteIdOrderByOrden(UUID reporteId);
}
