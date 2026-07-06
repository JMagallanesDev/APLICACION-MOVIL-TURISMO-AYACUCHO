package com.huamanga.tourism.lugar.dto;

import com.huamanga.tourism.lugar.dominio.EstadoLugar;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Alta/edición de lugar (RF-47): datos generales + traducciones + horarios
 * estructurados + datos prácticos "Antes de ir" (RF-09d).
 * Bounds de Ayacucho validados aquí (RF-22b) y re-verificados por CHECK en BD.
 */
public record LugarRequest(

        @NotBlank(message = "el slug es obligatorio")
        @Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*",
                 message = "el slug solo admite minúsculas, números y guiones")
        @Size(max = 150)
        String slug,

        @NotNull(message = "la categoría es obligatoria")
        UUID categoriaLugarId,

        @NotNull(message = "el distrito es obligatorio")
        UUID distritoId,

        @NotNull(message = "la latitud es obligatoria")
        @DecimalMin(value = "-15.5", message = "latitud fuera de la región Ayacucho")
        @DecimalMax(value = "-12.5", message = "latitud fuera de la región Ayacucho")
        Double latitud,

        @NotNull(message = "la longitud es obligatoria")
        @DecimalMin(value = "-75.5", message = "longitud fuera de la región Ayacucho")
        @DecimalMax(value = "-73.0", message = "longitud fuera de la región Ayacucho")
        Double longitud,

        @Size(max = 255)
        String direccion,

        @Size(max = 30)
        String telefono,

        @PositiveOrZero(message = "el precio no puede ser negativo")
        BigDecimal precioEntradaPen,

        @Positive(message = "la duración de visita debe ser positiva")
        Integer duracionVisitaMin,

        // Bloque "Antes de ir" (RF-09d) — nullables: desconocido ≠ no
        Boolean aceptaTarjeta,
        Boolean tieneBanos,
        Boolean accesibleSillaRuedas,
        Boolean aptoNinos,

        @PositiveOrZero(message = "el costo de taxi no puede ser negativo")
        BigDecimal costoTaxiDesdePlazaPen,

        Boolean requiereGuia,

        @NotNull(message = "el estado es obligatorio")
        EstadoLugar estado,

        @NotEmpty(message = "se requiere al menos la traducción en español")
        List<@Valid TraduccionLugarDto> traducciones,

        List<@Valid HorarioDto> horarios
) {

    @AssertTrue(message = "la traducción en español (es) es obligatoria")
    public boolean isConTraduccionEnEspanol() {
        return traducciones != null
                && traducciones.stream().anyMatch(t -> "es".equals(t.idioma()));
    }

    @AssertTrue(message = "no puede haber dos traducciones con el mismo idioma")
    public boolean isSinIdiomasDuplicados() {
        if (traducciones == null) {
            return true;
        }
        return traducciones.stream().map(TraduccionLugarDto::idioma).distinct().count()
                == traducciones.size();
    }
}
