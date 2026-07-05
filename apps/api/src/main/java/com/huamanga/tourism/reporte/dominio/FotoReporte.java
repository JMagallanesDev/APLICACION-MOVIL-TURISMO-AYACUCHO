package com.huamanga.tourism.reporte.dominio;
import com.huamanga.tourism.common.dominio.EntidadBase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "foto_reporte")
@Getter
@Setter
public class FotoReporte extends EntidadBase {

    @Column(name = "reporte_id", nullable = false)
    private UUID reporteId;

    @Column(name = "cloudinary_url", nullable = false, length = 500)
    private String cloudinaryUrl;

    @Column(name = "cloudinary_public_id", length = 255)
    private String cloudinaryPublicId;

    @Column(nullable = false)
    private int orden;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
