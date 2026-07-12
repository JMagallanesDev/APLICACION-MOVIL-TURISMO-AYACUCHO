package com.huamanga.tourism.usuario.service;

import com.huamanga.tourism.common.exception.NegocioException;
import com.huamanga.tourism.common.security.UsuarioAutenticado;
import com.huamanga.tourism.foto.dominio.EstadoFoto;
import com.huamanga.tourism.foto.dominio.Foto;
import com.huamanga.tourism.foto.repository.FotoRepository;
import com.huamanga.tourism.resena.dominio.EstadoResena;
import com.huamanga.tourism.resena.dominio.Resena;
import com.huamanga.tourism.resena.repository.ResenaRepository;
import com.huamanga.tourism.usuario.dominio.ReporteContenido;
import com.huamanga.tourism.usuario.repository.ReporteContenidoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Reporte de contenido inapropiado (RF-45): exactamente una FK no nula
 * (CHECK en BD, anti-patrón polimórfico descartado en 6.6); reporte duplicado
 * devuelve 409 (UNIQUE parciales); al 3er reporte único el contenido pasa a
 * EN_REVISION y desaparece del público hasta que el admin decida.
 */
@Service
public class ReporteContenidoService {

    private static final int UMBRAL_REVISION = 3;

    private final ReporteContenidoRepository reporteRepository;
    private final FotoRepository fotoRepository;
    private final ResenaRepository resenaRepository;

    public ReporteContenidoService(ReporteContenidoRepository reporteRepository,
                                   FotoRepository fotoRepository,
                                   ResenaRepository resenaRepository) {
        this.reporteRepository = reporteRepository;
        this.fotoRepository = fotoRepository;
        this.resenaRepository = resenaRepository;
    }

    @Transactional
    public void reportar(UUID fotoId, UUID resenaId, String motivo, UsuarioAutenticado usuario) {
        boolean esFoto = fotoId != null;
        boolean esResena = resenaId != null;
        if (esFoto == esResena) { // ninguna o ambas
            throw new NegocioException(HttpStatus.BAD_REQUEST, "OBJETIVO_INVALIDO",
                    "Debes reportar una foto o una reseña (solo una).");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "MOTIVO_REQUERIDO",
                    "Indica el motivo del reporte.");
        }

        if (esFoto) {
            reportarFoto(fotoId, motivo.trim(), usuario);
        } else {
            reportarResena(resenaId, motivo.trim(), usuario);
        }
    }

    private void reportarFoto(UUID fotoId, String motivo, UsuarioAutenticado usuario) {
        Foto foto = fotoRepository.findById(fotoId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND,
                        "FOTO_NO_ENCONTRADA", "La foto no existe."));
        if (reporteRepository.existsByUsuarioIdAndFotoId(usuario.id(), fotoId)) {
            throw yaReportado();
        }
        guardar(usuario.id(), fotoId, null, motivo);

        if (reporteRepository.countByFotoId(fotoId) >= UMBRAL_REVISION
                && foto.getEstado() == EstadoFoto.APROBADA) {
            foto.setEstado(EstadoFoto.EN_REVISION);
            fotoRepository.save(foto);
        }
    }

    private void reportarResena(UUID resenaId, String motivo, UsuarioAutenticado usuario) {
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND,
                        "RESENA_NO_ENCONTRADA", "La reseña no existe."));
        if (reporteRepository.existsByUsuarioIdAndResenaId(usuario.id(), resenaId)) {
            throw yaReportado();
        }
        guardar(usuario.id(), null, resenaId, motivo);

        if (reporteRepository.countByResenaId(resenaId) >= UMBRAL_REVISION
                && resena.getEstado() == EstadoResena.PUBLICADA) {
            resena.setEstado(EstadoResena.EN_REVISION);
            resenaRepository.save(resena);
        }
    }

    private void guardar(UUID usuarioId, UUID fotoId, UUID resenaId, String motivo) {
        ReporteContenido reporte = new ReporteContenido();
        reporte.setUsuarioId(usuarioId);
        reporte.setFotoId(fotoId);
        reporte.setResenaId(resenaId);
        reporte.setMotivo(motivo);
        reporteRepository.save(reporte);
    }

    private NegocioException yaReportado() {
        return new NegocioException(HttpStatus.CONFLICT, "YA_REPORTADO",
                "Ya reportaste este contenido; el equipo lo revisará.");
    }
}
