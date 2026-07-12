package com.huamanga.tourism.usuario.repository;

import com.huamanga.tourism.usuario.dominio.ReporteContenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReporteContenidoRepository extends JpaRepository<ReporteContenido, UUID> {

    boolean existsByUsuarioIdAndFotoId(UUID usuarioId, UUID fotoId);

    boolean existsByUsuarioIdAndResenaId(UUID usuarioId, UUID resenaId);

    /** La regla "3 reportes = revisión" se evalúa con COUNT indexado (plan 10.3). */
    long countByFotoId(UUID fotoId);

    long countByResenaId(UUID resenaId);
}
