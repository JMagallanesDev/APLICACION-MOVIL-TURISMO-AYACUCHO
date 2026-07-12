package com.huamanga.tourism.negocio.repository;

import com.huamanga.tourism.negocio.dominio.NegocioTraduccion;
import com.huamanga.tourism.negocio.dominio.NegocioTraduccionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface NegocioTraduccionRepository
        extends JpaRepository<NegocioTraduccion, NegocioTraduccionId> {

    List<NegocioTraduccion> findByNegocioId(UUID negocioId);

    List<NegocioTraduccion> findByNegocioIdIn(Collection<UUID> negocioIds);

    @Modifying
    @Query("DELETE FROM NegocioTraduccion t WHERE t.negocioId = :negocioId")
    void deleteByNegocioId(UUID negocioId);
}
