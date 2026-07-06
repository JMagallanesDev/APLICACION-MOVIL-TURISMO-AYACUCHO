package com.huamanga.tourism.foto.service;

import com.huamanga.tourism.common.exception.NegocioException;
import com.huamanga.tourism.common.integration.CloudinaryService;
import com.huamanga.tourism.common.security.UsuarioAutenticado;
import com.huamanga.tourism.foto.dominio.EstadoFoto;
import com.huamanga.tourism.foto.dominio.Foto;
import com.huamanga.tourism.foto.dto.FotoResponse;
import com.huamanga.tourism.foto.repository.FotoRepository;
import com.huamanga.tourism.lugar.dominio.Lugar;
import com.huamanga.tourism.lugar.repository.LugarRepository;
import com.huamanga.tourism.usuario.dominio.Usuario;
import com.huamanga.tourism.usuario.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Fotos de usuarios (RF-38): nacen PENDIENTE y solo la galería pública
 * muestra APROBADAS. RNF-15: validación MIME real (magic bytes, no extensión)
 * y tamaño máximo 5 MB. Límite de 5 fotos vivas por usuario y lugar.
 */
@Service
public class FotoService {

    private static final long TAMANO_MAXIMO_BYTES = 5 * 1024 * 1024;
    private static final int MAXIMO_FOTOS_POR_LUGAR = 5;

    private final FotoRepository fotoRepository;
    private final LugarRepository lugarRepository;
    private final UsuarioRepository usuarioRepository;
    private final CloudinaryService cloudinary;

    public FotoService(FotoRepository fotoRepository,
                       LugarRepository lugarRepository,
                       UsuarioRepository usuarioRepository,
                       CloudinaryService cloudinary) {
        this.fotoRepository = fotoRepository;
        this.lugarRepository = lugarRepository;
        this.usuarioRepository = usuarioRepository;
        this.cloudinary = cloudinary;
    }

    @Transactional(readOnly = true)
    public Page<FotoResponse> galeriaPublica(String slug, Pageable pageable) {
        Lugar lugar = lugarPorSlug(slug);
        Page<Foto> pagina = fotoRepository
                .findByLugarIdAndEstadoOrderByCreatedAtDesc(lugar.getId(), EstadoFoto.APROBADA, pageable);
        Map<UUID, String> nombres = nombresDe(pagina.getContent());
        return pagina.map(foto -> new FotoResponse(
                foto.getId(), foto.getCloudinaryUrl(), null, null,
                nombres.getOrDefault(foto.getUsuarioId(), "Visitante"), foto.getCreatedAt()));
    }

    @Transactional
    public FotoResponse subir(String slug, MultipartFile archivo, UsuarioAutenticado usuario) {
        if (!cloudinary.estaDisponible()) {
            throw new NegocioException(HttpStatus.SERVICE_UNAVAILABLE, "FOTOS_NO_DISPONIBLES",
                    "La subida de fotos no está disponible por el momento.");
        }
        Lugar lugar = lugarPorSlug(slug);

        long vivas = fotoRepository.countByUsuarioIdAndLugarIdAndEstadoIn(
                usuario.id(), lugar.getId(),
                List.of(EstadoFoto.PENDIENTE, EstadoFoto.APROBADA, EstadoFoto.EN_REVISION));
        if (vivas >= MAXIMO_FOTOS_POR_LUGAR) {
            throw new NegocioException(HttpStatus.CONFLICT, "LIMITE_FOTOS",
                    "Ya tienes " + MAXIMO_FOTOS_POR_LUGAR + " fotos para este lugar.");
        }

        byte[] contenido = validarImagen(archivo);
        CloudinaryService.ImagenSubida subida =
                cloudinary.subir(contenido, "lugares/" + lugar.getSlug());

        Foto foto = new Foto();
        foto.setUsuarioId(usuario.id());
        foto.setLugarId(lugar.getId());
        foto.setCloudinaryUrl(subida.url());
        foto.setCloudinaryPublicId(subida.publicId());
        foto.setEstado(EstadoFoto.PENDIENTE); // RF-38: siempre pasa por moderación
        fotoRepository.save(foto);

        return new FotoResponse(foto.getId(), foto.getCloudinaryUrl(), foto.getEstado(),
                null, usuario.email(), foto.getCreatedAt());
    }

    /** Moderación (RF-49): aprobar o rechazar con motivo. */
    @Transactional
    public FotoResponse moderar(UUID fotoId, EstadoFoto nuevoEstado, String motivoRechazo) {
        if (nuevoEstado != EstadoFoto.APROBADA && nuevoEstado != EstadoFoto.RECHAZADA) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "ESTADO_INVALIDO",
                    "La moderación solo admite APROBADA o RECHAZADA.");
        }
        Foto foto = fotoRepository.findById(fotoId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND,
                        "FOTO_NO_ENCONTRADA", "La foto no existe."));
        foto.setEstado(nuevoEstado);
        foto.setMotivoRechazo(nuevoEstado == EstadoFoto.RECHAZADA ? motivoRechazo : null);
        fotoRepository.save(foto);
        return new FotoResponse(foto.getId(), foto.getCloudinaryUrl(), foto.getEstado(),
                foto.getMotivoRechazo(), null, foto.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public Page<FotoResponse> bandejaModeracion(EstadoFoto estado, Pageable pageable) {
        Page<Foto> pagina = fotoRepository.findByEstadoOrderByCreatedAtAsc(estado, pageable);
        Map<UUID, String> nombres = nombresDe(pagina.getContent());
        return pagina.map(foto -> new FotoResponse(
                foto.getId(), foto.getCloudinaryUrl(), foto.getEstado(), foto.getMotivoRechazo(),
                nombres.getOrDefault(foto.getUsuarioId(), "Visitante"), foto.getCreatedAt()));
    }

    // ------------------------------------------------------------------

    /**
     * RNF-15: el tipo real se verifica por los primeros bytes del archivo
     * (magic bytes), nunca por la extensión ni el Content-Type declarado.
     */
    private byte[] validarImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "ARCHIVO_VACIO",
                    "Debes adjuntar una imagen.");
        }
        if (archivo.getSize() > TAMANO_MAXIMO_BYTES) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "ARCHIVO_MUY_GRANDE",
                    "La imagen no puede superar 5 MB.");
        }
        byte[] contenido;
        try {
            contenido = archivo.getBytes();
        } catch (IOException e) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "ARCHIVO_ILEGIBLE",
                    "No pudimos leer el archivo.");
        }
        if (!esJpeg(contenido) && !esPng(contenido) && !esWebp(contenido)) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "FORMATO_NO_SOPORTADO",
                    "Solo se aceptan imágenes JPG, PNG o WebP.");
        }
        return contenido;
    }

    private static boolean esJpeg(byte[] b) {
        return b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
    }

    private static boolean esPng(byte[] b) {
        return b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
    }

    private static boolean esWebp(byte[] b) {
        return b.length > 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
    }

    private Lugar lugarPorSlug(String slug) {
        return lugarRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND,
                        "LUGAR_NO_ENCONTRADO", "El lugar solicitado no existe."));
    }

    private Map<UUID, String> nombresDe(List<Foto> fotos) {
        List<UUID> ids = fotos.stream().map(Foto::getUsuarioId).distinct().toList();
        return usuarioRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNombre, (a, b) -> a));
    }
}
