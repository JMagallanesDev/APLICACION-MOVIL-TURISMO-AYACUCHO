package com.huamanga.tourism.common.security;

import java.util.UUID;

/** Principal que viaja en el SecurityContext tras validar el JWT. */
public record UsuarioAutenticado(UUID id, String email, String rol) {
}
