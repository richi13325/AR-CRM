package com.ar.crm2.application.ficha.command;

import java.util.UUID;

/**
 * Command to delete an existing Ficha.
 * Validates id at construction time.
 */
public record DeleteFichaCommand(
    UUID id
) {

    public DeleteFichaCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}