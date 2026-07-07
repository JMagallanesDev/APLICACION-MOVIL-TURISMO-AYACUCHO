package com.huamanga.tourism.reporte.service;

import com.huamanga.tourism.common.exception.NegocioException;
import com.huamanga.tourism.common.integration.CloudinaryService;
import com.huamanga.tourism.reporte.dominio.EstadoReporte;
import com.huamanga.tourism.reporte.dominio.FotoReporte;
import com.huamanga.tourism.reporte.dominio.Reporte;
import com.huamanga.tourism.reporte.dominio.TipoIncidente;
import com.huamanga.tourism.reporte.dto.IncidenteMapaResponse;
import com.huamanga.tourism.reporte.dto.ReporteRequest;
import com.huamanga.tourism.reporte.dto.ReporteResponse;
import com.huamanga.tourism.reporte.repository.FotoReporteRepository;
import com.huamanga.tourism.reporte.repository.ReporteRepository;
import com.huamanga.tourism.reporte.repository.TipoIncidenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Preservación ciudadana (módulo diferenciador). Privacidad por diseño:
 * NUNCA se persiste IP ni hash de IP; el anti-spam es un contador volátil en
 * Redis con TTL 24 h que se autodestruye (sección 6.6 y 10.4 del plan).
 */
@Service
public class ReporteService {

    private static final Logger log = LoggerFactory.getLogger(ReporteService.class);
    private static final GeometryFactory GEOMETRIAS =
            new GeometryFactory(new PrecisionModel(), 4326);
    private static final int MAX_FOTOS = 5;
    private static final int MAX_REPORTES_POR_IP_DIA = 10;
    /** Incidentes visibles en el mapa público (RF-74). */
    private static final List<EstadoReporte> PUBLICOS =
            List.of(EstadoReporte.APROBADO, EstadoReporte.RESUELTO);

    private final ReporteRepository reporteRepository;
    private final FotoReporteRepository fotoReporteRepository;
    private final TipoIncidenteRepository tipoRepository;
    private final CloudinaryService cloudinary;
    private final StringRedisTemplate redis;

    public ReporteService(ReporteRepository reporteRepository,
                          FotoReporteRepository fotoReporteRepository,
                          TipoIncidenteRepository tipoRepository,
                          CloudinaryService cloudinary,
                          StringRedisTemplate redis) {
        this.reporteRepository = reporteRepository;
        this.fotoReporteRepository = fotoReporteRepository;
        this.tipoRepository = tipoRepository;
        this.cloudinary = cloudinary;
        this.redis = redis;
    }

    /**
     * Crea un reporte (RF-69). usuarioId es nullable: los reportes anónimos
     * (RF-72) no lo llevan. ipEfimera solo se usa para el contador anti-spam
     * en Redis y jamás se guarda.
     */
    @Transactional
    public ReporteResponse crear(ReporteRequest request, List<MultipartFile> fotos,
                                 UUID usuarioId, String ipEfimera) {
        aplicarAntiSpam(ipEfimera);

        TipoIncidente tipo = tipoRepository.findById(request.tipoIncidenteId())
                .orElseThrow(() -> new NegocioException(HttpStatus.BAD_REQUEST,
                        "TIPO_INCIDENTE_INVALIDO", "El tipo de incidente no existe."));

        Reporte reporte = new Reporte();
        reporte.setTipoIncidenteId(tipo.getId());
        reporte.setDescripcion(request.descripcion().trim());
        reporte.setUbicacion(punto(request.longitud(), request.latitud()));
        reporte.setDireccionReferencial(request.direccionReferencial());
        reporte.setEsAnonimo(request.esAnonimo());
        reporte.setEstado(EstadoReporte.RECIBIDO);
        // Anónimo => sin usuario ni nombre; identificado => se conserva el nombre dado
        if (!request.esAnonimo()) {
            reporte.setUsuarioId(usuarioId);
            reporte.setNombreReportante(request.nombreReportante());
        }
        reporteRepository.save(reporte);

        List<String> urls = subirFotos(reporte.getId(), fotos);
        return aResponse(reporte, tipo.getCodigo(), urls);
    }

    /** Mapa público de incidentes aprobados (RF-74). */
    @Transactional(readOnly = true)
    public List<IncidenteMapaResponse> mapaPublico() {
        Map<UUID, TipoIncidente> tipos = tipoRepository.findAll().stream()
                .collect(Collectors.toMap(TipoIncidente::getId, Function.identity()));
        return reporteRepository.findByEstadoIn(PUBLICOS).stream().map(r -> {
            TipoIncidente tipo = tipos.get(r.getTipoIncidenteId());
            return new IncidenteMapaResponse(
                    r.getId(),
                    tipo != null ? tipo.getCodigo() : null,
                    tipo != null ? tipo.getIcono() : null,
                    tipo != null ? tipo.getColorHex() : null,
                    r.getUbicacion().getY(),
                    r.getUbicacion().getX(),
                    r.getEstado());
        }).toList();
    }

    /** Bandeja de moderación (RF-76). */
    @Transactional(readOnly = true)
    public Page<ReporteResponse> bandeja(EstadoReporte estado, Pageable pageable) {
        Map<UUID, String> codigos = tipoRepository.findAll().stream()
                .collect(Collectors.toMap(TipoIncidente::getId, TipoIncidente::getCodigo));
        return reporteRepository.findByEstadoOrderByCreatedAtAsc(estado, pageable)
                .map(r -> aResponse(r, codigos.get(r.getTipoIncidenteId()),
                        fotoReporteRepository.findByReporteIdOrderByOrden(r.getId()).stream()
                                .map(FotoReporte::getCloudinaryUrl).toList()));
    }

    @Transactional
    public void moderar(UUID reporteId, EstadoReporte nuevoEstado, String notasAdmin) {
        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND,
                        "REPORTE_NO_ENCONTRADO", "El reporte no existe."));
        reporte.setEstado(nuevoEstado);
        if (notasAdmin != null && !notasAdmin.isBlank()) {
            reporte.setNotasAdmin(notasAdmin.trim());
        }
        reporteRepository.save(reporte);
    }

    // ------------------------------------------------------------------

    /**
     * Contador volátil por IP con TTL 24 h (privacidad por diseño): la IP solo
     * forma la clave efímera de Redis, nunca se persiste. Fail-open si Redis
     * no responde (disponibilidad sobre bloqueo).
     */
    private void aplicarAntiSpam(String ipEfimera) {
        if (ipEfimera == null || ipEfimera.isBlank()) {
            return;
        }
        try {
            String clave = "antispam:reporte:" + ipEfimera;
            Long total = redis.opsForValue().increment(clave);
            if (total != null && total == 1L) {
                redis.expire(clave, Duration.ofHours(24));
            }
            if (total != null && total > MAX_REPORTES_POR_IP_DIA) {
                throw new NegocioException(HttpStatus.TOO_MANY_REQUESTS, "DEMASIADOS_REPORTES",
                        "Has enviado demasiados reportes hoy. Intenta de nuevo mañana.");
            }
        } catch (NegocioException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Anti-spam de reportes no disponible (Redis): se permite. {}", e.getMessage());
        }
    }

    private List<String> subirFotos(UUID reporteId, List<MultipartFile> fotos) {
        if (fotos == null || fotos.isEmpty() || !cloudinary.estaDisponible()) {
            if (fotos != null && !fotos.isEmpty() && !cloudinary.estaDisponible()) {
                log.warn("Cloudinary no disponible: reporte {} guardado sin fotos", reporteId);
            }
            return List.of();
        }
        List<String> urls = new java.util.ArrayList<>();
        int orden = 0;
        for (MultipartFile archivo : fotos) {
            if (orden >= MAX_FOTOS || archivo.isEmpty()) {
                break;
            }
            try {
                CloudinaryService.ImagenSubida subida =
                        cloudinary.subir(archivo.getBytes(), "reportes/" + reporteId);
                FotoReporte foto = new FotoReporte();
                foto.setReporteId(reporteId);
                foto.setCloudinaryUrl(subida.url());
                foto.setCloudinaryPublicId(subida.publicId());
                foto.setOrden(orden++);
                fotoReporteRepository.save(foto);
                urls.add(subida.url());
            } catch (Exception e) {
                log.warn("No se pudo subir una foto del reporte {}: {}", reporteId, e.getMessage());
            }
        }
        return urls;
    }

    private ReporteResponse aResponse(Reporte r, String tipoCodigo, List<String> fotos) {
        return new ReporteResponse(
                r.getId(), tipoCodigo, r.getDescripcion(),
                r.getUbicacion().getY(), r.getUbicacion().getX(),
                r.getDireccionReferencial(), r.getEstado(), r.isEsAnonimo(),
                fotos, r.getCreatedAt());
    }

    private static Point punto(double longitud, double latitud) {
        return GEOMETRIAS.createPoint(new Coordinate(longitud, latitud));
    }
}
