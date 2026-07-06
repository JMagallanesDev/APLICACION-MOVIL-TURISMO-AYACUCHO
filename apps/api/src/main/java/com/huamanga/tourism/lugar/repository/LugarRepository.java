package com.huamanga.tourism.lugar.repository;

import com.huamanga.tourism.lugar.dominio.EstadoLugar;
import com.huamanga.tourism.lugar.dominio.Lugar;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LugarRepository extends JpaRepository<Lugar, UUID> {

    Optional<Lugar> findBySlugAndDeletedAtIsNull(String slug);

    Optional<Lugar> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsBySlug(String slug);

    Page<Lugar> findByEstadoAndDeletedAtIsNull(EstadoLugar estado, Pageable pageable);

    Page<Lugar> findByEstadoAndCategoriaLugarIdAndDeletedAtIsNull(
            EstadoLugar estado, UUID categoriaLugarId, Pageable pageable);

    long countByEstadoAndDeletedAtIsNull(EstadoLugar estado);

    long countByCategoriaLugarIdAndDeletedAtIsNull(UUID categoriaLugarId);
}
