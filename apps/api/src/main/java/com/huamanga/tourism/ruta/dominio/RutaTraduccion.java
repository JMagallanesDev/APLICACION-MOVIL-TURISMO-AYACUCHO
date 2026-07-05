package com.huamanga.tourism.ruta.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ruta_traduccion")
@IdClass(RutaTraduccionId.class)
@Getter
@Setter
public class RutaTraduccion {

    @Id
    @Column(name = "ruta_tematica_id")
    private UUID rutaTematicaId;

    @Id
    @Column(length = 5)
    private String idioma;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
