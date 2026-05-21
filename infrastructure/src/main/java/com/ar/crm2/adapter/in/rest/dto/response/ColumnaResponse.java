package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.model.entity.Columna;

import java.util.UUID;

/**
 * REST response DTO for Columna.
 * Exposes all fields needed for front-end list/create/edit views.
 */
public record ColumnaResponse(
    UUID id,
    String nombre,
    String color,
    String tipoTablero,
    String tipoColumna
) {
    /**
     * Maps a domain Columna to this response DTO.
     */
    public static ColumnaResponse fromDomain(Columna columna) {
        return new ColumnaResponse(
            columna.getId().value(),
            columna.getColumnanombre(),
            columna.getColor(),
            columna.getTipoTablero().name(),
            columna.getTipoColumna().name()
        );
    }
}