package com.huamanga.tourism.horario;

import com.huamanga.tourism.common.EntidadAuditada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Horario estructurado por día y turno (RF-09b/RF-08/RF-29). Varias filas por
 * día = varios turnos (mañana/tarde). Texto libre sería dato no computable.
 */
@Entity
@Table(name = "horario_lugar")
@Getter
@Setter
public class HorarioLugar extends EntidadAuditada {

    @Column(name = "lugar_id", nullable = false)
    private UUID lugarId;

    /** 0=domingo ... 6=sábado */
    @Column(name = "dia_semana", nullable = false)
    private short diaSemana;

    @Column(name = "hora_apertura")
    private LocalTime horaApertura;

    @Column(name = "hora_cierre")
    private LocalTime horaCierre;

    @Column(nullable = false)
    private boolean cerrado;

    @Column(name = "updated_by")
    private UUID updatedBy;
}
