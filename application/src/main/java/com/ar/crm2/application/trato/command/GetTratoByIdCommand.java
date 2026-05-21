package com.ar.crm2.application.trato.command;

import java.util.UUID;

/**
 * Command to retrieve a Trato by its id.
 * Validates id at construction time.
 */
public record GetTratoByIdCommand(
    UUID id
) {

    public GetTratoByIdCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}