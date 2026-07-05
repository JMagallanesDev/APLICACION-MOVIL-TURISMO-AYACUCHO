package com.huamanga.tourism.lugar.repository;

import com.huamanga.tourism.lugar.dominio.LugarTraduccion;
import com.huamanga.tourism.lugar.dominio.LugarTraduccionId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LugarTraduccionRepository extends JpaRepository<LugarTraduccion, LugarTraduccionId> {

    List<LugarTraduccion> findByLugarId(UUID lugarId);

    Optional<LugarTraduccion> findByLugarIdAndIdioma(UUID lugarId, String idioma);
}
