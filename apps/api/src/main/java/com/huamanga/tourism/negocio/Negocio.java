package com.huamanga.tourism.negocio;

import com.huamanga.tourism.common.EntidadAuditada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.UUID;

/**
 * Nace PENDIENTE hasta aprobación del admin (RF-104). La descripción vive en
 * NegocioTraduccion (consistencia i18n).
 */
@Entity
@Table(name = "negocio")
@Getter
@Setter
public class Negocio extends EntidadAuditada {

    /** Gestor del negocio (rol NEGOCIO). */
    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "categoria_negocio_id", nullable = false)
    private UUID categoriaNegocioId;

    @Column(name = "distrito_id", nullable = false)
    private UUID distritoId;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 11)
    private String ruc;

    @Column(length = 30)
    private String telefono;

    @Column(length = 20)
    private String whatsapp; // RF-110

    @Column(length = 255)
    private String direccion;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point ubicacion;

    /** Texto simple: el negocio no participa del cálculo "abierto ahora". */
    @Column(length = 255)
    private String horario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoNegocio estado = EstadoNegocio.PENDIENTE;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
