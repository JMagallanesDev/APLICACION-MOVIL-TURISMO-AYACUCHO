package com.huamanga.tourism.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Seguridad stateless (plan, secciones 5.2/5.3): JWT por petición, sin
 * sesiones en memoria. La autorización por rol se verifica en cada endpoint
 * protegido (RNF-16) vía reglas aquí + @PreAuthorize en controllers.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthFilter jwtAuthFilter,
                                           RateLimitFilter rateLimitFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // stateless: sin cookies de sesión; el refresh cookie se limita a /auth
                .cors(cors -> {})
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Público: navegación anónima (RF-34) y salud
                        .requestMatchers("/health", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login",
                                         "/auth/refresh", "/auth/logout").permitAll()
                        // Documentación del API (Swagger UI)
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Catálogo público de lugares (RF-01, RF-09); escribir exige ADMIN (@PreAuthorize)
                        .requestMatchers(HttpMethod.GET, "/lugares/**").permitAll()
                        // Clima y recomendaciones: navegación anónima (RF-25, RF-08)
                        .requestMatchers(HttpMethod.GET, "/clima/**", "/recomendaciones/**").permitAll()
                        // Preservación ciudadana: reporte anónimo y mapa público (RF-69/72/74)
                        .requestMatchers(HttpMethod.GET, "/tipos-incidente", "/reportes/mapa").permitAll()
                        .requestMatchers(HttpMethod.POST, "/reportes").permitAll()
                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> {
                    res.setStatus(401);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write(
                            "{\"error\":\"NO_AUTENTICADO\",\"mensaje\":\"Debes iniciar sesión para acceder a este recurso.\"}");
                }))
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /** RNF-12: BCrypt cost 12. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /** CORS con credenciales: el frontend envía la cookie httpOnly a /auth. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${seguridad.cors-allowed-origins}") String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origenes.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
