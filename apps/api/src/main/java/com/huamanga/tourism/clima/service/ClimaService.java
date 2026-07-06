package com.huamanga.tourism.clima.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huamanga.tourism.clima.dto.ClimaResponse;
import com.huamanga.tourism.common.exception.NegocioException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;

/**
 * Clima actual (RF-25): OpenWeatherMap con caché Redis de 30 min y circuit
 * breaker Resilience4j. Si el proveedor cae, se sirve el último dato conocido
 * con aviso de antigüedad: el Home nunca se rompe por un tercero caído (5.4).
 */
@Service
public class ClimaService {

    private static final Logger log = LoggerFactory.getLogger(ClimaService.class);
    private static final String CLAVE_FRESCO = "clima:actual:";
    private static final String CLAVE_ULTIMO = "clima:ultimo:";

    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private final RestClient rest;
    private final String apiKey;
    private final double latitud;
    private final double longitud;
    private final int cacheMinutos;

    public ClimaService(StringRedisTemplate redis,
                        ObjectMapper json,
                        RestClient.Builder builder,
                        @Value("${clima.api-key}") String apiKey,
                        @Value("${clima.latitud}") double latitud,
                        @Value("${clima.longitud}") double longitud,
                        @Value("${clima.cache-minutos}") int cacheMinutos) {
        this.redis = redis;
        this.json = json;
        this.apiKey = apiKey;
        this.latitud = latitud;
        this.longitud = longitud;
        this.cacheMinutos = cacheMinutos;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.rest = builder.requestFactory(factory).build();
    }

    public ClimaResponse actual(String idioma) {
        // 1) Caché fresco (30 min): la mayoría de peticiones nunca sale de Redis
        ClimaResponse enCache = leer(CLAVE_FRESCO + idioma);
        if (enCache != null) {
            return enCache;
        }
        // 2) Sin API key configurada: servir último conocido o declarar no disponible
        if (apiKey == null || apiKey.isBlank()) {
            return ultimoConocidoODisponible(idioma, null);
        }
        // 3) Proveedor con circuito: si falla o está abierto, cae al fallback
        return consultarProveedor(idioma);
    }

    @CircuitBreaker(name = "clima", fallbackMethod = "fallbackUltimoConocido")
    public ClimaResponse consultarProveedor(String idioma) {
        JsonNode owm = rest.get()
                .uri("https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&units=metric&lang={lang}&appid={key}",
                        latitud, longitud, idioma, apiKey)
                .retrieve()
                .body(JsonNode.class);

        // Códigos OWM: 2xx tormenta, 3xx llovizna, 5xx lluvia (6xx nieve no aplica a Huamanga)
        int codigo = owm.path("weather").path(0).path("id").asInt();
        boolean lluvia = codigo >= 200 && codigo < 600;
        ClimaResponse clima = new ClimaResponse(
                Math.round(owm.path("main").path("temp").asDouble() * 10) / 10.0,
                Math.round(owm.path("main").path("feels_like").asDouble() * 10) / 10.0,
                owm.path("main").path("humidity").asInt(),
                owm.path("weather").path(0).path("description").asText(""),
                owm.path("weather").path(0).path("icon").asText(""),
                lluvia,
                0,
                false);

        guardar(CLAVE_FRESCO + idioma, clima, Duration.ofMinutes(cacheMinutos));
        guardar(CLAVE_ULTIMO + idioma, clima, null); // sin TTL: reserva para fallback
        return clima;
    }

    /** Invocado por Resilience4j cuando el proveedor falla o el circuito está abierto. */
    ClimaResponse fallbackUltimoConocido(String idioma, Throwable causa) {
        log.warn("OpenWeatherMap no disponible ({}): sirviendo último clima conocido",
                causa.getMessage());
        return ultimoConocidoODisponible(idioma, causa);
    }

    private ClimaResponse ultimoConocidoODisponible(String idioma, Throwable causa) {
        ClimaResponse ultimo = leer(CLAVE_ULTIMO + idioma);
        if (ultimo != null) {
            return ultimo;
        }
        throw new NegocioException(HttpStatus.SERVICE_UNAVAILABLE, "CLIMA_NO_DISPONIBLE",
                "El clima no está disponible por el momento.");
    }

    // ------------------------------------------------------------------

    private void guardar(String clave, ClimaResponse clima, Duration ttl) {
        try {
            ObjectNode nodo = json.valueToTree(clima);
            nodo.put("obtenidoEn", Instant.now().toEpochMilli());
            if (ttl != null) {
                redis.opsForValue().set(clave, nodo.toString(), ttl);
            } else {
                redis.opsForValue().set(clave, nodo.toString());
            }
        } catch (Exception e) {
            log.warn("No se pudo cachear el clima: {}", e.getMessage());
        }
    }

    private ClimaResponse leer(String clave) {
        try {
            String crudo = redis.opsForValue().get(clave);
            if (crudo == null) {
                return null;
            }
            JsonNode nodo = json.readTree(crudo);
            long antiguedadMin = Duration.between(
                    Instant.ofEpochMilli(nodo.path("obtenidoEn").asLong()), Instant.now()).toMinutes();
            boolean esFallback = clave.startsWith(CLAVE_ULTIMO);
            return new ClimaResponse(
                    nodo.path("temperaturaC").asDouble(),
                    nodo.path("sensacionC").asDouble(),
                    nodo.path("humedad").asInt(),
                    nodo.path("descripcion").asText(),
                    nodo.path("icono").asText(),
                    nodo.path("lluvia").asBoolean(),
                    antiguedadMin,
                    esFallback);
        } catch (Exception e) {
            log.warn("No se pudo leer clima cacheado: {}", e.getMessage());
            return null;
        }
    }
}
