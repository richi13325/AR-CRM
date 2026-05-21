package com.ar.crm2.application.rol.command;

import java.util.UUID;

/**
 * Command to delete an existing Rol.
 * Validates id at construction time.
 */
public record DeleteRolCommand(
    UUID id
) {

    public DeleteRolCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}