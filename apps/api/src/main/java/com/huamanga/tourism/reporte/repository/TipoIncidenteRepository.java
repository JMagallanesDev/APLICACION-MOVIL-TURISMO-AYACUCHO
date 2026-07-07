package com.huamanga.tourism.reporte.repository;

import com.huamanga.tourism.reporte.dominio.TipoIncidente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TipoIncidenteRepository extends JpaRepository<TipoIncidente, UUID> {
}
