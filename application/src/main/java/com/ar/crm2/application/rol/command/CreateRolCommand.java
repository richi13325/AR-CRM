package com.ar.crm2.application.rol.command;

import java.util.UUID;

/**
 * Command to create a new Rol.
 * Required fields validated at construction time.
 */
public record CreateRolCommand(
    String nombre,
    String descripcion
) {

    public CreateRolCommand {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
    }
}