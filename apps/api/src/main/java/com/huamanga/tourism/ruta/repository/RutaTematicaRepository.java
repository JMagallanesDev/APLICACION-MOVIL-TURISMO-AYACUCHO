package com.huamanga.tourism.ruta.repository;

import com.huamanga.tourism.ruta.dominio.RutaTematica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RutaTematicaRepository extends JpaRepository<RutaTematica, UUID> {

    List<RutaTematica> findByActivaTrueAndDeletedAtIsNullOrderByOrdenAsc();
}
