package com.huamanga.tourism.common.integration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Subida de imágenes a Cloudinary (RF-38/RF-48). Se configura con la variable
 * CLOUDINARY_URL (cloudinary://key:secret@cloud); sin ella el servicio se
 * declara no disponible y los endpoints de fotos responden 503.
 */
@Service
public class CloudinaryService {

    /** Resultado mínimo que persistimos: URL pública + public_id para borrado. */
    public record ImagenSubida(String url, String publicId) {
    }

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${CLOUDINARY_URL:}") String cloudinaryUrl) {
        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
            this.cloudinary = null;
            log.info("Cloudinary no configurado: subida de fotos deshabilitada");
        } else {
            this.cloudinary = new Cloudinary(cloudinaryUrl);
        }
    }

    public boolean estaDisponible() {
        return cloudinary != null;
    }

    /** Sube la imagen a la carpeta indicada con optimización automática. */
    public ImagenSubida subir(byte[] contenido, String carpeta) {
        try {
            Map<?, ?> resultado = cloudinary.uploader().upload(contenido, ObjectUtils.asMap(
                    "folder", carpeta,
                    "resource_type", "image",
                    // Entrega optimizada (RNF-03): formato y calidad automáticos
                    "transformation", "f_auto,q_auto"));
            return new ImagenSubida(
                    (String) resultado.get("secure_url"),
                    (String) resultado.get("public_id"));
        } catch (Exception e) {
            throw new IllegalStateException("Fallo al subir imagen a Cloudinary", e);
        }
    }
}
