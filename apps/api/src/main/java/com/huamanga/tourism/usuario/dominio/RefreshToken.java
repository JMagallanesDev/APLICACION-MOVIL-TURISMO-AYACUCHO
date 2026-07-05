package com.huamanga.tourism.usuario.dominio;
import com.huamanga.tourism.common.dominio.EntidadBase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/** Hasheado en BD con rotación en cada renovación (plan, sección 5.3). */
@Entity
@Table(name = "refresh_token")
@Getter
@Setter
public class RefreshToken extends EntidadBase {

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 100)
    private String tokenHash;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
