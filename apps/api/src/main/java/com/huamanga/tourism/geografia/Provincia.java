package com.huamanga.tourism.geografia;

import com.huamanga.tourism.common.EntidadAuditada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "provincia")
@Getter
@Setter
public class Provincia extends EntidadAuditada {

    @Column(nullable = false, unique = true, length = 10)
    private String codigo; // UBIGEO

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private int orden;
}
