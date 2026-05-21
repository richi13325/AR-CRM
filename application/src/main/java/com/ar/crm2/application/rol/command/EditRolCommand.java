package com.ar.crm2.application.rol.command;

import java.util.UUID;

/**
 * Command to edit an existing Rol.
 * Validates id and nombre at construction time.
 * Does NOT include activo — preserved from the existing entity via Rol.reconstitute.
 */
public record EditRolCommand(
    UUID id,
    String nombre,
    String descripcion
) {

    public EditRolCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
    }
}