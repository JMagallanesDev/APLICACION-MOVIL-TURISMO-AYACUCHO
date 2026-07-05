package com.huamanga.tourism.auth.service;

import com.huamanga.tourism.auth.dto.LoginRequest;
import com.huamanga.tourism.auth.dto.RegisterRequest;
import com.huamanga.tourism.auth.dto.TokenResponse;
import com.huamanga.tourism.auth.dto.UsuarioResponse;
import com.huamanga.tourism.common.exception.NegocioException;
import com.huamanga.tourism.common.security.JwtService;
import com.huamanga.tourism.common.security.UsuarioAutenticado;
import com.huamanga.tourism.usuario.dominio.EstadoUsuario;
import com.huamanga.tourism.usuario.dominio.RefreshToken;
import com.huamanga.tourism.usuario.dominio.Rol;
import com.huamanga.tourism.usuario.dominio.Usuario;
import com.huamanga.tourism.usuario.repository.RefreshTokenRepository;
import com.huamanga.tourism.usuario.repository.RolRepository;
import com.huamanga.tourism.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Autenticación stateless (plan, sección 5.3):
 * - Access token JWT corto que el cliente guarda solo en memoria.
 * - Refresh token opaco de 7 días en cookie httpOnly, hasheado (SHA-256)
 *   en BD y con rotación en cada renovación.
 */
@Service
public class AuthService {

    /** Resultado interno: el refresh crudo va a la cookie, nunca al body. */
    public record ResultadoAuth(TokenResponse tokens, String refreshToken) {
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshExpiracionMs;

    public AuthService(UsuarioRepository usuarioRepository,
                       RolRepository rolRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       @Value("${jwt.refresh-expiration-ms}") long refreshExpiracionMs) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshExpiracionMs = refreshExpiracionMs;
    }

    @Transactional
    public ResultadoAuth registrar(RegisterRequest request) {
        String email = request.email().toLowerCase().trim();
        if (usuarioRepository.existsByEmail(email)) {
            throw new NegocioException(HttpStatus.CONFLICT, "EMAIL_YA_REGISTRADO",
                    "Ya existe una cuenta con ese email.");
        }
        Rol rolUsuario = rolRepository.findByNombre("USUARIO")
                .orElseThrow(() -> new IllegalStateException("Rol USUARIO no existe en el seed"));

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setNombre(request.nombre().trim());
        usuario.setRolId(rolUsuario.getId());
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(usuario);

        return emitirTokens(usuario, "USUARIO");
    }

    @Transactional
    public ResultadoAuth login(LoginRequest request) {
        Usuario usuario = usuarioRepository
                .findByEmailAndDeletedAtIsNull(request.email().toLowerCase().trim())
                .orElseThrow(this::credencialesInvalidas);

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw credencialesInvalidas();
        }
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new NegocioException(HttpStatus.FORBIDDEN, "CUENTA_INACTIVA",
                    "Tu cuenta está suspendida o eliminada. Contacta al administrador.");
        }
        return emitirTokens(usuario, nombreRol(usuario));
    }

    /** Rotación: el refresh usado se invalida y se emite uno nuevo (5.3). */
    @Transactional
    public ResultadoAuth refrescar(String refreshTokenCrudo) {
        if (refreshTokenCrudo == null || refreshTokenCrudo.isBlank()) {
            throw sesionExpirada();
        }
        RefreshToken almacenado = refreshTokenRepository
                .findByTokenHash(sha256(refreshTokenCrudo))
                .orElseThrow(this::sesionExpirada);

        refreshTokenRepository.delete(almacenado);
        if (almacenado.getExpiraEn().isBefore(Instant.now())) {
            throw sesionExpirada();
        }
        Usuario usuario = usuarioRepository.findById(almacenado.getUsuarioId())
                .filter(u -> u.getDeletedAt() == null && u.getEstado() == EstadoUsuario.ACTIVO)
                .orElseThrow(this::sesionExpirada);

        return emitirTokens(usuario, nombreRol(usuario));
    }

    @Transactional
    public void logout(String refreshTokenCrudo) {
        if (refreshTokenCrudo != null && !refreshTokenCrudo.isBlank()) {
            refreshTokenRepository.findByTokenHash(sha256(refreshTokenCrudo))
                    .ifPresent(refreshTokenRepository::delete);
        }
    }

    @Transactional(readOnly = true)
    public UsuarioResponse me(UsuarioAutenticado autenticado) {
        Usuario usuario = usuarioRepository.findById(autenticado.id())
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new NegocioException(HttpStatus.UNAUTHORIZED,
                        "NO_AUTENTICADO", "Tu cuenta ya no está disponible."));
        return new UsuarioResponse(usuario.getId(), usuario.getEmail(),
                usuario.getNombre(), nombreRol(usuario));
    }

    public long getRefreshExpiracionMs() {
        return refreshExpiracionMs;
    }

    // ------------------------------------------------------------------

    private ResultadoAuth emitirTokens(Usuario usuario, String rol) {
        String access = jwtService.generarAccessToken(usuario.getId(), usuario.getEmail(), rol);

        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        String refreshCrudo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken refresh = new RefreshToken();
        refresh.setUsuarioId(usuario.getId());
        refresh.setTokenHash(sha256(refreshCrudo));
        refresh.setExpiraEn(Instant.now().plusMillis(refreshExpiracionMs));
        refreshTokenRepository.save(refresh);

        TokenResponse tokens = new TokenResponse(access, "Bearer",
                jwtService.getExpiracionSegundos(),
                new UsuarioResponse(usuario.getId(), usuario.getEmail(), usuario.getNombre(), rol));
        return new ResultadoAuth(tokens, refreshCrudo);
    }

    private String nombreRol(Usuario usuario) {
        return rolRepository.findById(usuario.getRolId())
                .map(Rol::getNombre)
                .orElse("USUARIO");
    }

    private NegocioException credencialesInvalidas() {
        // Mensaje único para email inexistente y contraseña errada:
        // no revelar cuál falló (enumeración de cuentas)
        return new NegocioException(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS",
                "Email o contraseña incorrectos.");
    }

    private NegocioException sesionExpirada() {
        return new NegocioException(HttpStatus.UNAUTHORIZED, "SESION_EXPIRADA",
                "Tu sesión expiró. Inicia sesión nuevamente.");
    }

    private static String sha256(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
