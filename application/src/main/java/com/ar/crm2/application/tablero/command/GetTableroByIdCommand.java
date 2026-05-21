package com.ar.crm2.application.tablero.command;

import java.util.UUID;

/**
 * Command to retrieve a Tablero by its ID.
 * No additional fields needed — the ID is sufficient.
 */
public record GetTableroByIdCommand(
    UUID id
) {

    public GetTableroByIdCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}