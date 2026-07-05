package com.huamanga.tourism.usuario.dominio;
import com.huamanga.tourism.common.dominio.EntidadAuditada;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario extends EntidadAuditada {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash; // BCrypt cost 12 (RNF-12)

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "rol_id", nullable = false)
    private UUID rolId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    /** Soft delete: la cuenta queda inaccesible, el contenido se preserva. */
    @Column(name = "deleted_at")
    private Instant deletedAt;
}
