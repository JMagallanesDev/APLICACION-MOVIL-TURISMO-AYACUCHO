package com.huamanga.tourism.ruta.repository;

import com.huamanga.tourism.ruta.dominio.LugarRuta;
import com.huamanga.tourism.ruta.dominio.LugarRutaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface LugarRutaRepository extends JpaRepository<LugarRuta, LugarRutaId> {

    List<LugarRuta> findByRutaTematicaIdInOrderByOrdenAsc(Collection<UUID> rutaIds);
}
