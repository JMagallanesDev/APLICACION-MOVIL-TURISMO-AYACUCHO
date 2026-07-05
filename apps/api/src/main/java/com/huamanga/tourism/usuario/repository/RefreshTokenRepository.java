package com.huamanga.tourism.usuario.repository;

import com.huamanga.tourism.usuario.dominio.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.usuarioId = :usuarioId")
    void deleteByUsuarioId(UUID usuarioId);

    /** Limpieza programada (job con ShedLock en sprint posterior). */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiraEn < :ahora")
    int deleteExpirados(Instant ahora);
}
