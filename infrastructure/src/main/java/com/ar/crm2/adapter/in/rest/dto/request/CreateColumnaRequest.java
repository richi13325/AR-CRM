package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * REST request DTO for creating a new Columna.
 * Required fields validated at construction time.
 */
public record CreateColumnaRequest(
    UUID superUsuarioId,
    @NotBlank String nombre,
    String color,
    TipoTablero tipoTablero,
    TipoColumna tipoColumna
) {}