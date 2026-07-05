package com.huamanga.tourism.negocio;

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

/** Español obligatorio, inglés opcional en el MVP. */
@Entity
@Table(name = "negocio_traduccion")
@IdClass(NegocioTraduccionId.class)
@Getter
@Setter
public class NegocioTraduccion {

    @Id
    @Column(name = "negocio_id")
    private UUID negocioId;

    @Id
    @Column(length = 5)
    private String idioma;

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
