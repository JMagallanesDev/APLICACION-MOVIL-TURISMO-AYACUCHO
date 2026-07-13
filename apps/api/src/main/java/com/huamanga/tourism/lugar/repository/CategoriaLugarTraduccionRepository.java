package com.huamanga.tourism.lugar.repository;

import com.huamanga.tourism.lugar.dominio.CategoriaLugarTraduccion;
import com.huamanga.tourism.lugar.dominio.CategoriaLugarTraduccionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaLugarTraduccionRepository
        extends JpaRepository<CategoriaLugarTraduccion, CategoriaLugarTraduccionId> {

    List<CategoriaLugarTraduccion> findByIdioma(String idioma);
}
