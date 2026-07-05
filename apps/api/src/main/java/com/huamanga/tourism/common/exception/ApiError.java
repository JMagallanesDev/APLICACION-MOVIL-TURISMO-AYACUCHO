package com.huamanga.tourism.common.exception;

import java.time.Instant;

/** Contrato uniforme de error: mensajes claros en español (RNF-23). */
public record ApiError(String error, String mensaje, Instant timestamp) {

    public static ApiError de(String error, String mensaje) {
        return new ApiError(error, mensaje, Instant.now());
    }
}
