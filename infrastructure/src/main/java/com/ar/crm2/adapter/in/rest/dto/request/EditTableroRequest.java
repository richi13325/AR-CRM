package com.ar.crm2.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * REST request DTO for editing an existing Tablero.
 * Only nombre and descripcion are editable; tipoTablero and creadoEn are preserved.
 */
public record EditTableroRequest(
    @NotBlank(message = "nombre is required")
    @Size(min = 1, max = 100, message = "nombre must be 1-100 characters")
    String nombre,

    String descripcion
) {}