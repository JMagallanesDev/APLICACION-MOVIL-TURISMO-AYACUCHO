package com.huamanga.tourism.negocio.repository;

import com.huamanga.tourism.negocio.dominio.CategoriaNegocioTraduccion;
import com.huamanga.tourism.negocio.dominio.CategoriaNegocioTraduccionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaNegocioTraduccionRepository
        extends JpaRepository<CategoriaNegocioTraduccion, CategoriaNegocioTraduccionId> {

    List<CategoriaNegocioTraduccion> findByIdioma(String idioma);
}
