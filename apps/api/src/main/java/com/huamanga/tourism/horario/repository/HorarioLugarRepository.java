package com.huamanga.tourism.horario.repository;

import com.huamanga.tourism.horario.dominio.HorarioLugar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HorarioLugarRepository extends JpaRepository<HorarioLugar, UUID> {

    /** Turnos de un lugar para un día (base del cálculo "abierto ahora", RF-09b). */
    List<HorarioLugar> findByLugarIdAndDiaSemanaOrderByHoraApertura(UUID lugarId, short diaSemana);

    List<HorarioLugar> findByLugarIdOrderByDiaSemanaAscHoraAperturaAsc(UUID lugarId);
}
