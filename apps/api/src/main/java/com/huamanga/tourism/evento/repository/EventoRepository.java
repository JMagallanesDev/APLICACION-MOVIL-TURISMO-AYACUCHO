package com.huamanga.tourism.evento.repository;

import com.huamanga.tourism.evento.dominio.EstadoEvento;
import com.huamanga.tourism.evento.dominio.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventoRepository extends JpaRepository<Evento, UUID> {

    Optional<Evento> findByIdAndDeletedAtIsNull(UUID id);

    /** Próximos eventos publicados (RF-84): terminan hoy o después. */
    @Query("""
            SELECT e FROM Evento e
            WHERE e.deletedAt IS NULL AND e.estado = :estado AND e.fechaFin >= :desde
            ORDER BY e.fechaInicio ASC
            """)
    List<Evento> proximos(@Param("estado") EstadoEvento estado,
                          @Param("desde") LocalDate desde, Pageable pageable);

    /**
     * Eventos que se solapan con un rango de fechas (RF-84b "Durante mi visita"
     * y RF-79 calendario por mes): empiezan antes del fin y terminan tras el
     * inicio del rango.
     */
    @Query("""
            SELECT e FROM Evento e
            WHERE e.deletedAt IS NULL AND e.estado = :estado
              AND e.fechaInicio <= :hasta AND e.fechaFin >= :desde
            ORDER BY e.fechaInicio ASC
            """)
    List<Evento> enRango(@Param("estado") EstadoEvento estado,
                         @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    Page<Evento> findByEstadoAndDeletedAtIsNullOrderByFechaInicioDesc(
            EstadoEvento estado, Pageable pageable);
}
