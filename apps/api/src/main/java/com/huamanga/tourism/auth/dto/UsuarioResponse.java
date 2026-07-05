package com.huamanga.tourism.auth.dto;

import java.util.UUID;

/** Contrato público del usuario: jamás expone password_hash (sección 10.2). */
public record UsuarioResponse(UUID id, String email, String nombre, String rol) {
}
