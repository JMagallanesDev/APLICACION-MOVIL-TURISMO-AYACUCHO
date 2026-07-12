package com.huamanga.tourism.negocio.repository;

import com.huamanga.tourism.negocio.dominio.CategoriaNegocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoriaNegocioRepository extends JpaRepository<CategoriaNegocio, UUID> {
}
