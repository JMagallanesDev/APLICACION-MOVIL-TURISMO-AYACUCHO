package com.huamanga.tourism.foto;

import com.huamanga.tourism.common.EntidadAuditada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** Nace PENDIENTE (RF-38); la galería pública solo muestra APROBADAS. */
@Entity
@Table(name = "foto")
@Getter
@Setter
public class Foto extends EntidadAuditada {

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "lugar_id", nullable = false)
    private UUID lugarId;

    @Column(name = "cloudinary_url", nullable = false, length = 500)
    private String cloudinaryUrl;

    @Column(name = "cloudinary_public_id", nullable = false, length = 255)
    private String cloudinaryPublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoFoto estado = EstadoFoto.PENDIENTE;

    @Column(name = "motivo_rechazo", length = 300)
    private String motivoRechazo;
}
