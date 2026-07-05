package com.huamanga.tourism.negocio.dominio;

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
public class CategoriaNegocioTraduccionId implements Serializable {
    private UUID categoriaNegocioId;
    private String idioma;
}
