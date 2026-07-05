package com.huamanga.tourism.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * RNF-14: rate limiting de 100 req/min por IP real del cliente.
 * - IP real: primer valor de X-Forwarded-For (tras proxy confiable de
 *   Vercel/Railway); sin esa cabecera, la IP remota directa.
 * - Contador volátil en Redis con TTL de 60 s (misma filosofía que el
 *   anti-spam de reportes: nada se persiste en BD).
 * - Fail-open: si Redis no responde, la petición pasa (disponibilidad
 *   sobre límite; coherente con la degradación elegante de la sección 5.4).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final StringRedisTemplate redis;
    private final int limitePorMinuto;

    public RateLimitFilter(StringRedisTemplate redis,
                           @Value("${seguridad.rate-limit-por-minuto}") int limitePorMinuto) {
        this.redis = redis;
        this.limitePorMinuto = limitePorMinuto;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String ip = ipRealDelCliente(request);
            String clave = "rl:" + ip + ":" + (System.currentTimeMillis() / 60_000);
            Long total = redis.opsForValue().increment(clave);
            if (total != null && total == 1L) {
                redis.expire(clave, Duration.ofSeconds(60));
            }
            if (total != null && total > limitePorMinuto) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"error\":\"DEMASIADAS_PETICIONES\",\"mensaje\":\"Has superado el límite de peticiones. Intenta de nuevo en un minuto.\"}");
                return;
            }
        } catch (Exception e) {
            log.warn("Rate limit no disponible (Redis caído): se permite la petición. {}", e.getMessage());
        }
        chain.doFilter(request, response);
    }

    private String ipRealDelCliente(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // Primer valor = IP original del cliente; validación básica de formato
            String primera = xff.split(",")[0].trim();
            if (primera.matches("[0-9a-fA-F:.]{3,45}")) {
                return primera;
            }
        }
        return request.getRemoteAddr();
    }
}
