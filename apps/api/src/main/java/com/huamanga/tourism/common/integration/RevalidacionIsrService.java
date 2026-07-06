package com.huamanga.tourism.common.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Dispara la revalidación ISR en Vercel al guardar contenido (sección 5.5).
 * Fire-and-forget resiliente: si el webhook falla, el contenido igual quedó
 * en BD y las páginas se regeneran por el revalidate por tiempo; jamás se
 * bloquea ni se rompe la operación del admin por un tercero caído (5.4).
 * Si REVALIDATE_WEBHOOK_URL no está configurada (dev/tests), es un no-op.
 */
@Service
public class RevalidacionIsrService {

    private static final Logger log = LoggerFactory.getLogger(RevalidacionIsrService.class);

    private final String webhookUrl;
    private final String secreto;
    private final RestClient rest;

    public RevalidacionIsrService(
            @Value("${REVALIDATE_WEBHOOK_URL:}") String webhookUrl,
            @Value("${REVALIDATE_SECRET:}") String secreto,
            RestClient.Builder builder) {
        this.webhookUrl = webhookUrl;
        this.secreto = secreto;
        this.rest = builder
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                    setConnectTimeout(Duration.ofSeconds(3));
                    setReadTimeout(Duration.ofSeconds(5));
                }})
                .build();
    }

    /** Revalida las rutas públicas de un lugar en ambos idiomas. */
    public void revalidarLugar(String slug) {
        revalidar(List.of(
                "/es/lugares", "/en/lugares",
                "/es/lugares/" + slug, "/en/lugares/" + slug));
    }

    public void revalidar(List<String> paths) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return; // dev/tests: sin webhook configurado
        }
        CompletableFuture.runAsync(() -> {
            try {
                rest.post()
                        .uri(webhookUrl)
                        .header("x-revalidate-secret", secreto)
                        .body(Map.of("paths", paths))
                        .retrieve()
                        .toBodilessEntity();
                log.info("Revalidación ISR disparada para {}", paths);
            } catch (Exception e) {
                log.warn("Webhook de revalidación ISR falló (se regenerará por tiempo): {}",
                        e.getMessage());
            }
        });
    }
}
