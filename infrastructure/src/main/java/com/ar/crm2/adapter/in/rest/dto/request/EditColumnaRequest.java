package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;

import java.util.UUID;

/**
 * REST request DTO for editing an existing Columna.
 */
public record EditColumnaRequest(
    String nombre,
    String color,
    TipoTablero tipoTablero,
    TipoColumna tipoColumna
) {}