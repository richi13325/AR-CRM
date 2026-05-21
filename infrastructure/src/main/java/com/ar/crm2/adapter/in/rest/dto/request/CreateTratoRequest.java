package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.TipoContrato;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * REST request DTO for creating a new Trato.
 * Required fields validated at construction time.
 * Timestamps (creadoEn, actualizadoEn) and id are never accepted from the client.
 */
public record CreateTratoRequest(
    @NotNull(message = "contactoId is required")
    UUID contactoId,

    @NotNull(message = "responsableId is required")
    UUID responsableId,

    @NotBlank(message = "nombre is required")
    @Size(min = 1, max = 200, message = "nombre must be 1-200 characters")
    String nombre,

    BigDecimal valorEstimado,

    @Min(value = 0, message = "probabilidad must be at least 0")
    @Max(value = 100, message = "probabilidad must be at most 100")
    Integer probabilidad,

    LocalDate fechaCierreEsperada,

    @NotNull(message = "tipoContrato is required")
    TipoContrato tipoContrato
) {}