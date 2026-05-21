package com.ar.crm2.application.usuario.command;

import java.util.UUID;

/**
 * Command to delete a Usuario by its id.
 */
public record DeleteUsuarioCommand(
    UUID id
) {

    public DeleteUsuarioCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}