package com.ar.crm2.adapter.in.rest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * REST request DTO for assigning an existing catalog Columna to a Tablero.
 *
 * <p>Context-only fields: WIP limit, note, and total estimated value.
 * Catalog data (nombre, color, tipoColumna) is NOT carried here —
 * it belongs to the Columna catalog entity.
 *
 * <p>This DTO is paired with {@code @RequestParam UUID columnaId} at the
 * controller level to identify the catalog column being assigned.
 */
public record AsignarColumnaRequest(
    @NotNull(message = "limiteWip is required")
    @Min(value = 1, message = "limiteWip must be at least 1")
    Integer limiteWip,

    @Size(max = 500, message = "nota must be at most 500 characters")
    String nota,

    @NotNull(message = "totalValorEstimado is required")
    BigDecimal totalValorEstimado
) {}
