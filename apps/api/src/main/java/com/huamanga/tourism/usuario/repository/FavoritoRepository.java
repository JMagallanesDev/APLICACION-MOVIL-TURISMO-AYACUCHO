package com.huamanga.tourism.usuario.repository;

import com.huamanga.tourism.usuario.dominio.Favorito;
import com.huamanga.tourism.usuario.dominio.FavoritoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, FavoritoId> {

    List<Favorito> findByUsuarioIdOrderByCreatedAtDesc(UUID usuarioId);

    boolean existsByUsuarioIdAndLugarId(UUID usuarioId, UUID lugarId);

    @Modifying
    @Query("DELETE FROM Favorito f WHERE f.usuarioId = :usuarioId AND f.lugarId = :lugarId")
    void eliminar(UUID usuarioId, UUID lugarId);
}
