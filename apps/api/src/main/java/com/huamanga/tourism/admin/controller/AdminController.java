package com.huamanga.tourism.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Verificación de autorización por rol (RNF-16). El panel real del admin
 * se construye en el sprint 9 del plan comprimido; este ping permite probar
 * el 403 desde ya.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> ping() {
        return Map.of("pong", true, "modulo", "admin");
    }
}
