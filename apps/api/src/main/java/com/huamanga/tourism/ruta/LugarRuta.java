package com.huamanga.tourism.ruta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** Pivot N:M con orden de visita. */
@Entity
@Table(name = "lugar_ruta")
@IdClass(LugarRutaId.class)
@Getter
@Setter
public class LugarRuta {

    @Id
    @Column(name = "ruta_tematica_id")
    private UUID rutaTematicaId;

    @Id
    @Column(name = "lugar_id")
    private UUID lugarId;

    @Column(nullable = false)
    private int orden;
}
