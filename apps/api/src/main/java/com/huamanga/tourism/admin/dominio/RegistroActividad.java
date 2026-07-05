package com.huamanga.tourism.admin.dominio;
import com.huamanga.tourism.common.dominio.EntidadBase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Log inmutable del admin (RF-56). Aquí la IP sí se guarda: es auditoría
 * interna del administrador, no del usuario público.
 */
@Entity
@Table(name = "registro_actividad")
@Getter
@Setter
public class RegistroActividad extends EntidadBase {

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false, length = 50)
    private String accion;

    @Column(nullable = false, length = 50)
    private String entidad;

    @Column(name = "entidad_id")
    private UUID entidadId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String detalles;

    @Column(length = 45)
    private String ip;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
