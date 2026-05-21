package com.ar.crm2.application.empresa.command;

import java.util.UUID;

/**
 * Command to delete an existing Empresa.
 * Validates id at construction time.
 */
public record DeleteEmpresaCommand(
    UUID id
) {

    public DeleteEmpresaCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}