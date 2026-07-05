package com.huamanga.tourism.auth.dto;

/**
 * El access token viaja en el body y el cliente lo guarda SOLO en memoria;
 * el refresh token nunca aparece aquí: va en cookie httpOnly (sección 5.3).
 */
public record TokenResponse(String accessToken, String tokenType,
                            long expiraEnSegundos, UsuarioResponse usuario) {
}
