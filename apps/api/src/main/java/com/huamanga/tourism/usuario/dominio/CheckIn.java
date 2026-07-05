package com.huamanga.tourism.usuario.dominio;
import com.huamanga.tourism.common.dominio.EntidadBase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.UUID;

/** Alimenta el pasaporte patrimonial (RF-39b) y la vista estadistica_lugar. */
@Entity
@Table(name = "check_in")
@Getter
@Setter
public class CheckIn extends EntidadBase {

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "lugar_id", nullable = false)
    private UUID lugarId;

    @Column(name = "ubicacion_gps", columnDefinition = "geometry(Point,4326)")
    private Point ubicacionGps;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
