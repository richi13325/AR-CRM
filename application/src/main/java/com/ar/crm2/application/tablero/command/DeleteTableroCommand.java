package com.ar.crm2.application.tablero.command;

import java.util.UUID;

/**
 * Command to delete an existing Tablero by its ID.
 */
public record DeleteTableroCommand(
    UUID id
) {

    public DeleteTableroCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}