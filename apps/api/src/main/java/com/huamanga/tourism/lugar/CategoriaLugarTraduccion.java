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

@Entity
@Table(name = "categoria_lugar_traduccion")
@IdClass(CategoriaLugarTraduccionId.class)
@Getter
@Setter
public class CategoriaLugarTraduccion {

    @Id
    @Column(name = "categoria_lugar_id")
    private UUID categoriaLugarId;

    @Id
    @Column(length = 5)
    private String idioma;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
