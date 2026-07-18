package com.huamanga.tourism.ruta.repository;

import com.huamanga.tourism.ruta.dominio.RutaTraduccion;
import com.huamanga.tourism.ruta.dominio.RutaTraduccionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface RutaTraduccionRepository extends JpaRepository<RutaTraduccion, RutaTraduccionId> {

    List<RutaTraduccion> findByRutaTematicaIdIn(Collection<UUID> rutaIds);
}
