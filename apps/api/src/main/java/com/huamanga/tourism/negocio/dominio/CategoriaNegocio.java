package com.huamanga.tourism.negocio.dominio;
import com.huamanga.tourism.common.dominio.EntidadAuditada;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Independiente de CategoriaLugar: dominios semánticamente distintos (6.6). */
@Entity
@Table(name = "categoria_negocio")
@Getter
@Setter
public class CategoriaNegocio extends EntidadAuditada {

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(length = 50)
    private String icono;

    @Column(nullable = false)
    private int orden;
}
