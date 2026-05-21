package com.ar.crm2.application.columna.command;

import java.util.UUID;

/**
 * Command to get a Columna by its id.
 * Validates id at construction time.
 */
public record GetColumnaByIdCommand(
    UUID id
) {

    public GetColumnaByIdCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}