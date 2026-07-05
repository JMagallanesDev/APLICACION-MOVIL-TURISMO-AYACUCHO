package com.huamanga.tourism.common.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de salud del Sprint 1: verifica conectividad con PostgreSQL y Redis.
 * Expuesto en GET /api/v1/health (context-path /api/v1).
 */
@RestController
public class HealthController {

    private final JdbcTemplate jdbc;
    private final StringRedisTemplate redis;

    public HealthController(JdbcTemplate jdbc, StringRedisTemplate redis) {
        this.jdbc = jdbc;
        this.redis = redis;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("app", "turismo-huamanga-api");
        body.put("timestamp", Instant.now().toString());

        boolean dbOk;
        try {
            Integer one = jdbc.queryForObject("SELECT 1", Integer.class);
            dbOk = Integer.valueOf(1).equals(one);
        } catch (Exception e) {
            dbOk = false;
        }
        body.put("database", dbOk ? "UP" : "DOWN");

        boolean redisOk;
        try {
            String pong = redis.getConnectionFactory().getConnection().ping();
            redisOk = "PONG".equalsIgnoreCase(pong);
        } catch (Exception e) {
            redisOk = false;
        }
        body.put("redis", redisOk ? "UP" : "DOWN");

        boolean allOk = dbOk && redisOk;
        body.put("status", allOk ? "UP" : "DEGRADED");
        return ResponseEntity.status(allOk ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}
