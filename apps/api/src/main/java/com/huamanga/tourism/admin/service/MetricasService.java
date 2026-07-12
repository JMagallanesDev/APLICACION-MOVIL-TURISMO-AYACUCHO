package com.huamanga.tourism.admin.service;

import com.huamanga.tourism.admin.dto.MetricasResponse;
import com.huamanga.tourism.foto.dominio.EstadoFoto;
import com.huamanga.tourism.foto.repository.FotoRepository;
import com.huamanga.tourism.lugar.dominio.EstadoLugar;
import com.huamanga.tourism.lugar.repository.LugarRepository;
import com.huamanga.tourism.negocio.dominio.EstadoNegocio;
import com.huamanga.tourism.negocio.repository.NegocioRepository;
import com.huamanga.tourism.reporte.dominio.EstadoReporte;
import com.huamanga.tourism.reporte.repository.ReporteRepository;
import com.huamanga.tourism.resena.dominio.EstadoResena;
import com.huamanga.tourism.resena.repository.ResenaRepository;
import com.huamanga.tourism.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Agregados en vivo para el dashboard (RF-52). */
@Service
public class MetricasService {

    private final LugarRepository lugarRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResenaRepository resenaRepository;
    private final FotoRepository fotoRepository;
    private final ReporteRepository reporteRepository;
    private final NegocioRepository negocioRepository;

    public MetricasService(LugarRepository lugarRepository,
                           UsuarioRepository usuarioRepository,
                           ResenaRepository resenaRepository,
                           FotoRepository fotoRepository,
                           ReporteRepository reporteRepository,
                           NegocioRepository negocioRepository) {
        this.lugarRepository = lugarRepository;
        this.usuarioRepository = usuarioRepository;
        this.resenaRepository = resenaRepository;
        this.fotoRepository = fotoRepository;
        this.reporteRepository = reporteRepository;
        this.negocioRepository = negocioRepository;
    }

    @Transactional(readOnly = true)
    public MetricasResponse resumen() {
        return new MetricasResponse(
                lugarRepository.countByEstadoAndDeletedAtIsNull(EstadoLugar.PUBLICADO),
                usuarioRepository.count(),
                resenaRepository.countByEstado(EstadoResena.PUBLICADA),
                fotoRepository.countByEstado(EstadoFoto.PENDIENTE),
                resenaRepository.countByEstado(EstadoResena.EN_REVISION),
                reporteRepository.countByEstado(EstadoReporte.RECIBIDO),
                reporteRepository.countByEstado(EstadoReporte.APROBADO),
                negocioRepository.countByEstadoAndDeletedAtIsNull(EstadoNegocio.PENDIENTE));
    }
}
