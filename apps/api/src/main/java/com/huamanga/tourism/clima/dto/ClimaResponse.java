package com.huamanga.tourism.clima.dto;

/**
 * Clima actual sobre Huamanga (RF-25). esFallback=true indica que el
 * proveedor está caído y se sirve el último dato conocido con su antigüedad
 * (degradación elegante, plan sección 5.4).
 */
public record ClimaResponse(
        double temperaturaC,
        double sensacionC,
        int humedad,
        String descripcion,
        String icono,
        boolean lluvia,
        long antiguedadMin,
        boolean esFallback
) {
}
