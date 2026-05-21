package com.ar.crm2.application.columna.command;

import java.util.UUID;

/**
 * Command to delete an existing Columna.
 * Validates id at construction time.
 */
public record DeleteColumnaCommand(
    UUID id
) {

    public DeleteColumnaCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}