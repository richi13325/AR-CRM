package com.ar.crm2.application.ficha.command;

import java.util.UUID;

/**
 * Command to retrieve a Ficha by its id.
 * Validates id at construction time.
 */
public record GetFichaByIdCommand(
    UUID id
) {

    public GetFichaByIdCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}