package com.huamanga.tourism.insignia;

import com.huamanga.tourism.common.EntidadAuditada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * El criterio JSONB declara la regla de obtención, p. ej.
 * {"tipo":"checkins_categoria","categoria":"IGLESIAS","cantidad":5}.
 * La evaluación corre en el service tras cada check-in (RF-39b).
 */
@Entity
@Table(name = "insignia")
@Getter
@Setter
public class Insignia extends EntidadAuditada {

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(length = 50)
    private String icono;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String criterio;

    @Column(nullable = false)
    private int orden;
}
