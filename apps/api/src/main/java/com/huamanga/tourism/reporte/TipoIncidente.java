package com.huamanga.tourism.reporte;

import com.huamanga.tourism.common.EntidadAuditada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 7 valores en seed V5 (RF-70). */
@Entity
@Table(name = "tipo_incidente")
@Getter
@Setter
public class TipoIncidente extends EntidadAuditada {

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(length = 50)
    private String icono;

    @Column(name = "color_hex", length = 7)
    private String colorHex;
}
