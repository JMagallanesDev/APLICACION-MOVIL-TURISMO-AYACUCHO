package com.huamanga.tourism.analitica;

import com.huamanga.tourism.common.EntidadAuditada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Tabla de hechos agregada por día (patrón data warehouse). Los eventos
 * crudos no se persisten por diseño (privacidad y volumen): esta tabla ES
 * la fuente primaria, por lo que cumple 3FN (plan, nota del Grupo 8).
 */
@Entity
@Table(name = "visita_resumen_diario",
       uniqueConstraints = @UniqueConstraint(name = "idx_visita_resumen_unique",
                                             columnNames = {"tipo_pagina", "fecha"}))
@Getter
@Setter
public class VisitaResumenDiario extends EntidadAuditada {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pagina", nullable = false, length = 30)
    private TipoPagina tipoPagina;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "total_visitas", nullable = false)
    private int totalVisitas;

    @Column(name = "visitas_unicas", nullable = false)
    private int visitasUnicas;
}
