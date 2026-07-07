package com.huamanga.tourism.common.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resuelve la IP real del cliente detrás del proxy confiable de
 * Vercel/Railway (RNF-14): primer valor de X-Forwarded-For, o la IP remota
 * directa. Uso EXCLUSIVAMENTE efímero (claves de Redis con TTL); nunca se
 * persiste en BD — ni siquiera hasheada (privacidad por diseño, sección 6.6).
 */
public final class ClienteIp {

    private ClienteIp() {
    }

    public static String resolver(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String primera = xff.split(",")[0].trim();
            if (primera.matches("[0-9a-fA-F:.]{3,45}")) {
                return primera;
            }
        }
        return request.getRemoteAddr();
    }
}
