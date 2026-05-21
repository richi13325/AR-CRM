package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoEstadoColumnaTableroTarea;
import com.ar.crm2.model.enums.TipoEstadoColumnaTableroTrato;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * REST request DTO for adding a column to an existing Tablero.
 *
 * @deprecated As of Wave 1 of the {@code columnas-en-tableros} change, this DTO is
 * retained for backward compatibility with the existing {@code /agregar-columna}
 * endpoint. New assignment flows should use {@link AsignarColumnaRequest} instead,
 * which operates on existing catalog columns without duplicating catalog fields
 * (nombre, color, tipoColumna) in the request body.
 *
 * <p>Both tarea-state and trato-state fields are present but are exclusive
 * based on the parent Tablero's tipoTablero.
 */
@Deprecated
public record AgregarColumnaRequest(
    @NotBlank(message = "nombre is required")
    @Size(min = 1, max = 80, message = "nombre must be 1-80 characters")
    String nombre,

    @Size(max = 7, message = "color must be at most 7 characters (e.g. #RRGGBB)")
    String color,

    @NotNull(message = "tipoColumna is required")
    TipoColumna tipoColumna,

    @NotNull(message = "limiteWip is required")
    @Min(value = 1, message = "limiteWip must be at least 1")
    Integer limiteWip,

    @Size(max = 500, message = "nota must be at most 500 characters")
    String nota,

    TipoEstadoColumnaTableroTarea estadoTarea,

    TipoEstadoColumnaTableroTrato estadoTrato,

    @NotNull(message = "totalValorEstimado is required")
    BigDecimal totalValorEstimado,

    boolean existeOtraColumnaConMismoNombre
) {}