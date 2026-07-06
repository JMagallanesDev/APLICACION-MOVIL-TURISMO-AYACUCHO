package com.huamanga.tourism.geografia.repository;

import com.huamanga.tourism.geografia.dominio.Distrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DistritoRepository extends JpaRepository<Distrito, UUID> {
}
