package com.ar.crm2.application.contacto.command;

import java.util.UUID;

/**
 * Command to delete an existing Contacto.
 * Validates id at construction time.
 */
public record DeleteContactoCommand(
    UUID id
) {

    public DeleteContactoCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}