package com.huamanga.tourism.evento.repository;

import com.huamanga.tourism.evento.dominio.EventoTraduccion;
import com.huamanga.tourism.evento.dominio.EventoTraduccionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventoTraduccionRepository
        extends JpaRepository<EventoTraduccion, EventoTraduccionId> {

    List<EventoTraduccion> findByEventoId(UUID eventoId);

    List<EventoTraduccion> findByEventoIdIn(Collection<UUID> eventoIds);

    @Modifying
    @Query("DELETE FROM EventoTraduccion t WHERE t.eventoId = :eventoId")
    void deleteByEventoId(UUID eventoId);
}
