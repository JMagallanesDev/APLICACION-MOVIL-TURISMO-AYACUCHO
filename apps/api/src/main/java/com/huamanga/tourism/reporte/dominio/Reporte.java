package com.huamanga.tourism.reporte.dominio;
import com.huamanga.tourism.common.dominio.EntidadAuditada;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

/**
 * Reporte ciudadano de atentado al patrimonio (módulo diferenciador).
 * NO almacena IP ni hash de IP bajo ninguna forma: el anti-spam vive
 * exclusivamente en Redis con TTL 24 h (privacidad por diseño, sección 6.6).
 */
@Entity
@Table(name = "reporte")
@Getter
@Setter
public class Reporte extends EntidadAuditada {

    @Column(name = "tipo_incidente_id", nullable = false)
    private UUID tipoIncidenteId;

    /** Nullable: reporte anónimo (RF-72). */
    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "nombre_reportante", length = 120)
    private String nombreReportante;

    @Column(nullable = false, columnDefinition = "text")
    private String descripcion;

    @Column(nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point ubicacion;

    @Column(name = "direccion_referencial", length = 255)
    private String direccionReferencial;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReporte estado = EstadoReporte.RECIBIDO;

    @Column(name = "notas_admin", columnDefinition = "text")
    private String notasAdmin;

    @Column(name = "es_anonimo", nullable = false)
    private boolean esAnonimo;
}
