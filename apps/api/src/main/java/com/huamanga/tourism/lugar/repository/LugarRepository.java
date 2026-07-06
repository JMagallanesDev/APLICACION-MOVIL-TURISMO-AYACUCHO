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

    Page<Lugar> findByEstadoAndCategoriaLugarIdAndDeletedAtIsNull(
            EstadoLugar estado, UUID categoriaLugarId, Pageable pageable);

    long countByEstadoAndDeletedAtIsNull(EstadoLugar estado);

    long countByCategoriaLugarIdAndDeletedAtIsNull(UUID categoriaLugarId);

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
