package com.huamanga.tourism.evento;

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

/** El organizador vive aquí para soportar traducción (plan, sección 6.2). */
@Entity
@Table(name = "evento_traduccion")
@IdClass(EventoTraduccionId.class)
@Getter
@Setter
public class EventoTraduccion {

    @Id
    @Column(name = "evento_id")
    private UUID eventoId;

    @Id
    @Column(length = 5)
    private String idioma;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(length = 150)
    private String organizador;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
