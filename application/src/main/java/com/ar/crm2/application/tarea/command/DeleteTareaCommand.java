package com.ar.crm2.application.tarea.command;

import java.util.UUID;

/**
 * Command to delete an existing Tarea.
 * Validates id at construction time.
 */
public record DeleteTareaCommand(
    UUID id
) {

    public DeleteTareaCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}