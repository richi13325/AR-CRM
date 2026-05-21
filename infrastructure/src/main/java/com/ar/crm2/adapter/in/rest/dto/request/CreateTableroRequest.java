package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.TipoTablero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * REST request DTO for creating a new Tablero.
 * Required fields: nombre, descripcion, tipoTablero, superUsuarioId.
 * Timestamps (creadoEn) and id are never accepted from the client.
 * The 4 default columns are synthesized by the application service, not provided by the client.
 */
public record CreateTableroRequest(
    @NotBlank(message = "nombre is required")
    @Size(min = 1, max = 100, message = "nombre must be 1-100 characters")
    String nombre,

    @NotBlank(message = "descripcion is required")
    String descripcion,

    @NotNull(message = "tipoTablero is required")
    TipoTablero tipoTablero,

    @NotNull(message = "superUsuarioId is required")
    UUID superUsuarioId,

    boolean columnasPredeterminadas
) {}