package com.ar.crm2.application.superusuario.command;

import java.util.UUID;

/**
 * Command to get a SuperUsuario by its id.
 */
public record GetSuperUsuarioByIdCommand(
    UUID id
) {

    public GetSuperUsuarioByIdCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}