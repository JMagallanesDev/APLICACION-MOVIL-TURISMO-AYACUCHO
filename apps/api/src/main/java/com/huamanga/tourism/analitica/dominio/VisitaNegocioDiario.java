package com.huamanga.tourism.analitica.dominio;
import com.huamanga.tourism.common.dominio.EntidadAuditada;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/** Base de medición del futuro plan premium (RF-52b / RF-111 del ERS). */
@Entity
@Table(name = "visita_negocio_diario",
       uniqueConstraints = @UniqueConstraint(name = "idx_visita_negocio_unique",
                                             columnNames = {"negocio_id", "fecha"}))
@Getter
@Setter
public class VisitaNegocioDiario extends EntidadAuditada {

    @Column(name = "negocio_id", nullable = false)
    private UUID negocioId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "total_visitas", nullable = false)
    private int totalVisitas;

    @Column(name = "clics_whatsapp", nullable = false)
    private int clicsWhatsapp;

    @Column(name = "clics_como_llegar", nullable = false)
    private int clicsComoLlegar;
}
