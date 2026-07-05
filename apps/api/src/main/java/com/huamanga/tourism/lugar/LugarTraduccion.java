package com.huamanga.tourism.lugar;

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

/** Agregar un idioma es un INSERT, no un ALTER TABLE (plan, sección 10.3). */
@Entity
@Table(name = "lugar_traduccion")
@IdClass(LugarTraduccionId.class)
@Getter
@Setter
public class LugarTraduccion {

    @Id
    @Column(name = "lugar_id")
    private UUID lugarId;

    @Id
    @Column(length = 5)
    private String idioma;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(columnDefinition = "text")
    private String historia;

    /** RF-09d, traducible */
    @Column(columnDefinition = "text")
    private String consejos;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
