package com.ar.crm2.application.columna.command;

import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;

import java.util.UUID;

/**
 * Command to edit an existing Columna.
 * Validates id, nombre, tipoTablero, tipoColumna at construction time.
 * Does NOT include superUsuarioId — preserved from the existing entity via Columna.reconstitute.
 */
public record EditColumnaCommand(
    UUID id,
    String nombre,
    String color,
    TipoTablero tipoTablero,
    TipoColumna tipoColumna
) {

    public EditColumnaCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
        if (tipoTablero == null) {
            throw new IllegalArgumentException("tipoTablero is required");
        }
        if (tipoColumna == null) {
            throw new IllegalArgumentException("tipoColumna is required");
        }
    }
}