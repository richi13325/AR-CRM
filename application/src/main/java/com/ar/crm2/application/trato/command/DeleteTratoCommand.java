package com.ar.crm2.application.trato.command;

import java.util.UUID;

/**
 * Command to delete an existing Trato.
 * Validates id at construction time.
 */
public record DeleteTratoCommand(
    UUID id
) {

    public DeleteTratoCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}