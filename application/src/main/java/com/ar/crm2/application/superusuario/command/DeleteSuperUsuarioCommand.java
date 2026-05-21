package com.ar.crm2.application.superusuario.command;

import java.util.UUID;

/**
 * Command to delete a SuperUsuario by its id.
 */
public record DeleteSuperUsuarioCommand(
    UUID id
) {

    public DeleteSuperUsuarioCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}