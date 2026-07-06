package com.huamanga.tourism.lugar.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalTime;

/** Un turno de un día. Varios turnos por día = varias filas (mañana/tarde). */
public record HorarioDto(

        @Min(value = 0, message = "dia_semana va de 0 (domingo) a 6 (sábado)")
        @Max(value = 6, message = "dia_semana va de 0 (domingo) a 6 (sábado)")
        short diaSemana,

        LocalTime horaApertura,
        LocalTime horaCierre,
        boolean cerrado
) {

    @AssertTrue(message = "un turno abierto requiere hora de apertura menor a la de cierre")
    public boolean isTurnoCoherente() {
        if (cerrado) {
            return true;
        }
        return horaApertura != null && horaCierre != null && horaApertura.isBefore(horaCierre);
    }
}
