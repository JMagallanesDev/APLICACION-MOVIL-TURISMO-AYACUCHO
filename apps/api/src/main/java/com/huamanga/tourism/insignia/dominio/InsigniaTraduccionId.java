package com.huamanga.tourism.insignia.dominio;

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
public class InsigniaTraduccionId implements Serializable {
    private UUID insigniaId;
    private String idioma;
}
