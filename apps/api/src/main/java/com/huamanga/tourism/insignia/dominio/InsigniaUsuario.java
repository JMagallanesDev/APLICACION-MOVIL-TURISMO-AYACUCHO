package com.huamanga.tourism.insignia.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Solo se persiste el hecho inmutable: insignia obtenida y su fecha.
 * El progreso por ruta NO se almacena (violaría 3FN): se calcula con COUNT
 * sobre check_in x lugar_ruta.
 */
@Entity
@Table(name = "insignia_usuario")
@IdClass(InsigniaUsuarioId.class)
@Getter
@Setter
public class InsigniaUsuario {

    @Id
    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Id
    @Column(name = "insignia_id")
    private UUID insigniaId;

    @CreationTimestamp
    @Column(name = "obtenida_en", nullable = false, updatable = false)
    private Instant obtenidaEn;
}
