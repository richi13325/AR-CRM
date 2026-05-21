package com.ar.crm2.application.tablero.command;

import java.util.UUID;

/**
 * Command to edit an existing Tablero's name and description.
 */
public record EditTableroCommand(
    UUID id,
    String nombre,
    String descripcion
) {

    public EditTableroCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("descripcion is required");
        }
    }
}