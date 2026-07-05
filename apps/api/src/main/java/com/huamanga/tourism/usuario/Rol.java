package com.huamanga.tourism.usuario;

import com.huamanga.tourism.common.EntidadAuditada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Valores: VISITANTE, USUARIO, NEGOCIO, ADMIN (seed V5). */
@Entity
@Table(name = "rol")
@Getter
@Setter
public class Rol extends EntidadAuditada {

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    @Column(length = 200)
    private String descripcion;
}
