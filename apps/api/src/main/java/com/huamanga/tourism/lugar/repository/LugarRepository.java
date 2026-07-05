package com.huamanga.tourism.lugar.repository;

import com.huamanga.tourism.lugar.dominio.EstadoLugar;
import com.huamanga.tourism.lugar.dominio.Lugar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LugarRepository extends JpaRepository<Lugar, UUID> {

    Optional<Lugar> findBySlugAndDeletedAtIsNull(String slug);

    long countByEstadoAndDeletedAtIsNull(EstadoLugar estado);

    long countByCategoriaLugarIdAndDeletedAtIsNull(UUID categoriaLugarId);
}
