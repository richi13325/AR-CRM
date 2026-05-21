package com.ar.crm2.application.usuario.command;

import java.util.UUID;

/**
 * Command to get a Usuario by its id.
 */
public record GetUsuarioByIdCommand(
    UUID id
) {

    public GetUsuarioByIdCommand {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
    }
}