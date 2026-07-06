package com.huamanga.tourism.foto.repository;

import com.huamanga.tourism.foto.dominio.EstadoFoto;
import com.huamanga.tourism.foto.dominio.Foto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface FotoRepository extends JpaRepository<Foto, UUID> {

    Page<Foto> findByLugarIdAndEstadoOrderByCreatedAtDesc(
            UUID lugarId, EstadoFoto estado, Pageable pageable);

    /** Bandeja de moderación (RF-49), usa el índice parcial idx_foto_pendientes. */
    Page<Foto> findByEstadoOrderByCreatedAtAsc(EstadoFoto estado, Pageable pageable);

    long countByUsuarioIdAndLugarIdAndEstadoIn(
            UUID usuarioId, UUID lugarId, Collection<EstadoFoto> estados);
}
