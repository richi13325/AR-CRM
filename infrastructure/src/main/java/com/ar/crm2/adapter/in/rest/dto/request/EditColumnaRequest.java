package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;
import jakarta.validation.constraints.NotBlank;

/**
 * REST request DTO for editing an existing Columna.
 */
public record EditColumnaRequest(
    @NotBlank String nombre,
    String color,
    TipoTablero tipoTablero,
    TipoColumna tipoColumna
) {}