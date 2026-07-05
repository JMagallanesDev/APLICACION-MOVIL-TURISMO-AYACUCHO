package com.huamanga.tourism.resena;

import com.huamanga.tourism.common.EntidadAuditada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Una reseña por usuario y lugar (UNIQUE). El promedio NO se almacena aquí ni
 * en Lugar: se lee de la vista materializada estadistica_lugar (3FN).
 */
@Entity
@Table(name = "resena",
       uniqueConstraints = @UniqueConstraint(name = "idx_resena_unique",
                                             columnNames = {"usuario_id", "lugar_id"}))
@Getter
@Setter
public class Resena extends EntidadAuditada {

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "lugar_id", nullable = false)
    private UUID lugarId;

    /** 1-5, CHECK en BD */
    @Column(nullable = false)
    private short calificacion;

    @Column(length = 500)
    private String comentario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoResena estado = EstadoResena.PUBLICADA;
}
