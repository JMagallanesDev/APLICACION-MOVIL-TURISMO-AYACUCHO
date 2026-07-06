package com.huamanga.tourism.lugar.repository;

import com.huamanga.tourism.lugar.dominio.CategoriaLugar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoriaLugarRepository extends JpaRepository<CategoriaLugar, UUID> {

    Optional<CategoriaLugar> findByCodigo(String codigo);
}
