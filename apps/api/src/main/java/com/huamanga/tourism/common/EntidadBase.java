package com.huamanga.tourism.common;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * PK UUID v7 generada en el backend con uuid-creator: ordenable por tiempo
 * (rendimiento de índice) y no expone el tamaño de la BD (plan, sección 10.3).
 */
@MappedSuperclass
@Getter
@Setter
public abstract class EntidadBase {

    @Id
    private UUID id;

    @PrePersist
    protected void generarId() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}
