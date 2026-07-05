package com.huamanga.tourism.geografia.dominio;
import com.huamanga.tourism.common.dominio.EntidadAuditada;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** La provincia se deriva del distrito: 3FN sin redundancia. */
@Entity
@Table(name = "distrito")
@Getter
@Setter
public class Distrito extends EntidadAuditada {

    @Column(name = "provincia_id", nullable = false)
    private UUID provinciaId;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo; // UBIGEO

    @Column(nullable = false, length = 100)
    private String nombre;
}
