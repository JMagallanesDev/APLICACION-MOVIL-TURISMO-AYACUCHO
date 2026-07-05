package com.huamanga.tourism.lugar;

import com.huamanga.tourism.common.EntidadAuditada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categoria_lugar")
@Getter
@Setter
public class CategoriaLugar extends EntidadAuditada {

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(length = 50)
    private String icono;

    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(nullable = false)
    private int orden;
}
