package com.huamanga.tourism.lugar.repository;

import com.huamanga.tourism.lugar.dominio.EstadoLugar;
import com.huamanga.tourism.lugar.dominio.Lugar;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LugarRepository extends JpaRepository<Lugar, UUID> {

    Optional<Lugar> findBySlugAndDeletedAtIsNull(String slug);

    Optional<Lugar> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsBySlug(String slug);

    Page<Lugar> findByEstadoAndDeletedAtIsNull(EstadoLugar estado, Pageable pageable);

    /** Gestión del admin: todos los estados, sin eliminados. */
    Page<Lugar> findByDeletedAtIsNullOrderBySlugAsc(Pageable pageable);

    Page<Lugar> findByEstadoAndCategoriaLugarIdAndDeletedAtIsNull(
            EstadoLugar estado, UUID categoriaLugarId, Pageable pageable);

    long countByEstadoAndDeletedAtIsNull(EstadoLugar estado);

    long countByCategoriaLugarIdAndDeletedAtIsNull(UUID categoriaLugarId);

    /**
     * Ranking "mejor valorados" (RF-06): ordena por la vista materializada
     * estadistica_lugar; los lugares sin reseñas van al final (NULLS LAST).
     * El Pageable debe llegar sin sort: el ORDER BY es parte de la query.
     */
    @Query(value = """
            SELECT l.* FROM lugar l
            LEFT JOIN estadistica_lugar e ON e.lugar_id = l.id
            WHERE l.deleted_at IS NULL AND l.estado = 'PUBLICADO'
              AND (CAST(:categoriaId AS uuid) IS NULL
                   OR l.categoria_lugar_id = CAST(:categoriaId AS uuid))
            ORDER BY e.calificacion_promedio DESC NULLS LAST,
                     e.total_resenas DESC NULLS LAST,
                     l.slug ASC
            """,
            countQuery = """
            SELECT count(*) FROM lugar l
            WHERE l.deleted_at IS NULL AND l.estado = 'PUBLICADO'
              AND (CAST(:categoriaId AS uuid) IS NULL
                   OR l.categoria_lugar_id = CAST(:categoriaId AS uuid))
            """,
            nativeQuery = true)
    Page<Lugar> listarPublicadosPorCalificacion(
            @Param("categoriaId") String categoriaId, Pageable pageable);

    /**
     * Búsqueda full-text (RF-02) sobre nombre+descripción en cualquier idioma,
     * usando el índice GIN idx_lugartrad_fulltext (RNF-30).
     */
    @Query(value = """
            SELECT l.* FROM lugar l
            WHERE l.deleted_at IS NULL AND l.estado = 'PUBLICADO'
              AND EXISTS (
                SELECT 1 FROM lugar_traduccion t
                WHERE t.lugar_id = l.id
                  AND to_tsvector('simple', t.nombre || ' ' || COALESCE(t.descripcion, ''))
                      @@ plainto_tsquery('simple', :q))
            """,
            countQuery = """
            SELECT count(*) FROM lugar l
            WHERE l.deleted_at IS NULL AND l.estado = 'PUBLICADO'
              AND EXISTS (
                SELECT 1 FROM lugar_traduccion t
                WHERE t.lugar_id = l.id
                  AND to_tsvector('simple', t.nombre || ' ' || COALESCE(t.descripcion, ''))
                      @@ plainto_tsquery('simple', :q))
            """,
            nativeQuery = true)
    Page<Lugar> buscarPublicados(@Param("q") String q, Pageable pageable);
}
