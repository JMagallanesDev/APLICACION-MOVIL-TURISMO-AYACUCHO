package com.huamanga.tourism.negocio.repository;

import com.huamanga.tourism.negocio.dominio.EstadoNegocio;
import com.huamanga.tourism.negocio.dominio.Negocio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NegocioRepository extends JpaRepository<Negocio, UUID> {

    /** Listado público (RF-105): solo aprobados. */
    Page<Negocio> findByEstadoAndDeletedAtIsNullOrderByNombreAsc(
            EstadoNegocio estado, Pageable pageable);

    Page<Negocio> findByEstadoAndCategoriaNegocioIdAndDeletedAtIsNullOrderByNombreAsc(
            EstadoNegocio estado, UUID categoriaNegocioId, Pageable pageable);

    /** Panel propio (RF-107): un negocio por usuario gestor en el MVP. */
    Optional<Negocio> findByUsuarioIdAndDeletedAtIsNull(UUID usuarioId);

    boolean existsByUsuarioIdAndDeletedAtIsNull(UUID usuarioId);

    /** Bandeja de moderación del admin. */
    Page<Negocio> findByEstadoAndDeletedAtIsNullOrderByCreatedAtAsc(
            EstadoNegocio estado, Pageable pageable);

    long countByEstadoAndDeletedAtIsNull(EstadoNegocio estado);
}
