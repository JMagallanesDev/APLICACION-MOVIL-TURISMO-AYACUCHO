package com.huamanga.tourism.evento.dominio;
import com.huamanga.tourism.common.dominio.EntidadAuditada;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Soporta clonado anual (RF-86) vía recurrente_anual. */
@Entity
@Table(name = "evento")
@Getter
@Setter
public class Evento extends EntidadAuditada {

    /** Opcional: un evento puede no ocurrir en un lugar registrado. */
    @Column(name = "lugar_id")
    private UUID lugarId;

    @Column(name = "distrito_id", nullable = false)
    private UUID distritoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEvento tipo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "cloudinary_url_portada", length = 500)
    private String cloudinaryUrlPortada;

    @Column(name = "recurrente_anual", nullable = false)
    private boolean recurrenteAnual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEvento estado = EstadoEvento.BORRADOR;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
