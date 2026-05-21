package com.ar.crm2.application.tarea.command;

import java.util.UUID;

/**
 * Command to retrieve a Tarea by its id.
 * Validates id at construction time.
 */
public record GetTareaByIdCommand(
    UUID id
) {

    public GetTareaByIdCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}