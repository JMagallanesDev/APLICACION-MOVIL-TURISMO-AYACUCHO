package com.huamanga.tourism.lugar;

import com.huamanga.tourism.common.EntidadAuditada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad central del dominio. Los horarios viven normalizados en
 * HorarioLugar (no como texto libre) y los textos en LugarTraduccion.
 */
@Entity
@Table(name = "lugar")
@Getter
@Setter
public class Lugar extends EntidadAuditada {

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(name = "categoria_lugar_id", nullable = false)
    private UUID categoriaLugarId;

    @Column(name = "distrito_id", nullable = false)
    private UUID distritoId;

    /** SRID 4326; bounds de Ayacucho garantizados por CHECK en BD (RF-22b). */
    @Column(nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point ubicacion;

    @Column(length = 255)
    private String direccion;

    @Column(length = 30)
    private String telefono;

    @Column(name = "precio_entrada_pen", precision = 8, scale = 2)
    private BigDecimal precioEntradaPen;

    @Column(name = "duracion_visita_min")
    private Integer duracionVisitaMin; // RF-09c

    // Bloque "Antes de ir" (RF-09d) — nullables: "desconocido" es distinto de "no"
    @Column(name = "acepta_tarjeta")
    private Boolean aceptaTarjeta;

    @Column(name = "tiene_banos")
    private Boolean tieneBanos;

    @Column(name = "accesible_silla_ruedas")
    private Boolean accesibleSillaRuedas;

    @Column(name = "apto_ninos")
    private Boolean aptoNinos;

    @Column(name = "costo_taxi_desde_plaza_pen", precision = 6, scale = 2)
    private BigDecimal costoTaxiDesdePlazaPen;

    @Column(name = "requiere_guia")
    private Boolean requiereGuia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoLugar estado = EstadoLugar.BORRADOR;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
