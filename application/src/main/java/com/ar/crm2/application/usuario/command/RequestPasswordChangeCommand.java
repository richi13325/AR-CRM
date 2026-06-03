package com.ar.crm2.application.usuario.command;

import java.util.UUID;

public record RequestPasswordChangeCommand(
    UUID usuarioId
) {

    public RequestPasswordChangeCommand {
        if (usuarioId == null) {
            throw new IllegalArgumentException("usuarioId is required");
        }
    }
}
