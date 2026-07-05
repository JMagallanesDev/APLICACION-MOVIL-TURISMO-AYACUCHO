package com.huamanga.tourism.lugar;

import com.huamanga.tourism.common.EntidadAuditada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

/** Slider antes/después (RF-11) con punto de captura "Párate aquí" (RF-11b). */
@Entity
@Table(name = "lugar_imagen_historica")
@Getter
@Setter
public class LugarImagenHistorica extends EntidadAuditada {

    @Column(name = "lugar_id", nullable = false)
    private UUID lugarId;

    @Column(length = 150)
    private String titulo;

    @Column(name = "url_historica", nullable = false, length = 500)
    private String urlHistorica;

    @Column(name = "public_id_historica", length = 255)
    private String publicIdHistorica;

    /** Límite fijo 1500-1990 en BD; la regla "≥ 50 años" va en Bean Validation. */
    @Column(name = "anio_historico")
    private Integer anioHistorico;

    @Column(name = "url_actual", length = 500)
    private String urlActual;

    @Column(name = "public_id_actual", length = 255)
    private String publicIdActual;

    @Column(name = "credito_historico", length = 255)
    private String creditoHistorico;

    @Column(name = "punto_captura", columnDefinition = "geometry(Point,4326)")
    private Point puntoCaptura;

    @Column(nullable = false)
    private int orden;
}
