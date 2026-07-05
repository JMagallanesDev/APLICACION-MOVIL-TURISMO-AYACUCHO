package com.huamanga.tourism.reporte.dominio;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TipoIncidenteTraduccionId implements Serializable {
    private UUID tipoIncidenteId;
    private String idioma;
}
