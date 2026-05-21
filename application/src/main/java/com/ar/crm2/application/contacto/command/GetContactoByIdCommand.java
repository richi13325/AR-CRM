package com.ar.crm2.application.contacto.command;

import java.util.UUID;

/**
 * Command to retrieve a Contacto by its id.
 * Validates id at construction time.
 */
public record GetContactoByIdCommand(
    UUID id
) {

    public GetContactoByIdCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}