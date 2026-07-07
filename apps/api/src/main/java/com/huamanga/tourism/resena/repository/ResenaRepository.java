package com.huamanga.tourism.resena.repository;

import com.huamanga.tourism.resena.dominio.EstadoResena;
import com.huamanga.tourism.resena.dominio.Resena;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, UUID> {

    Page<Resena> findByLugarIdAndEstadoOrderByCreatedAtDesc(
            UUID lugarId, EstadoResena estado, Pageable pageable);

    Page<Resena> findByEstadoOrderByCreatedAtDesc(EstadoResena estado, Pageable pageable);

    boolean existsByUsuarioIdAndLugarId(UUID usuarioId, UUID lugarId);

    long countByEstado(EstadoResena estado);
}
