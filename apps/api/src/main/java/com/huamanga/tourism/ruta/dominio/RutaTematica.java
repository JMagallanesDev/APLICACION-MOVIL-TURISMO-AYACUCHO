package com.huamanga.tourism.ruta.dominio;
import com.huamanga.tourism.common.dominio.EntidadAuditada;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ruta_tematica")
@Getter
@Setter
public class RutaTematica extends EntidadAuditada {

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(length = 50)
    private String icono;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(nullable = false)
    private int orden;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
