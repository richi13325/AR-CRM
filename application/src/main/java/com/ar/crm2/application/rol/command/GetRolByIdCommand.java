package com.ar.crm2.application.rol.command;

import java.util.UUID;

/**
 * Command to get a Rol by its id.
 * Validates id at construction time.
 */
public record GetRolByIdCommand(
    UUID id
) {

    public GetRolByIdCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}