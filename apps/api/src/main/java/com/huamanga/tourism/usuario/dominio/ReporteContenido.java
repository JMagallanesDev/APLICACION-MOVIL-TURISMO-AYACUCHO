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

/**
 * Dos FKs nullables + CHECK de exactamente una no nula (en BD) en lugar de
 * FK polimórfica — anti-patrón descartado en la sección 6.6 del plan.
 * Al tercer reporte único el contenido pasa a EN_REVISION (RF-45).
 */
@Entity
@Table(name = "reporte_contenido")
@Getter
@Setter
public class ReporteContenido extends EntidadBase {

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "foto_id")
    private UUID fotoId;

    @Column(name = "resena_id")
    private UUID resenaId;

    @Column(nullable = false, length = 300)
    private String motivo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
