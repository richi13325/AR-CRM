package com.ar.crm2.application.etiqueta.command;

import java.util.UUID;

/**
 * Command to edit an existing Etiqueta.
 */
public record EditEtiquetaCommand(
    UUID id,
    String nombre,
    String color
) {

    public EditEtiquetaCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
        if (color == null || color.isBlank()) {
            throw new IllegalArgumentException("color is required");
        }
    }
}
