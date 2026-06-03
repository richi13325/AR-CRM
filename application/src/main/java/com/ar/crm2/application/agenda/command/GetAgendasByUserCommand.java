package com.ar.crm2.application.agenda.command;

import java.util.UUID;

public record GetAgendasByUserCommand(UUID usuarioId) {
    public GetAgendasByUserCommand {
        if (usuarioId == null) {
            throw new IllegalArgumentException("usuarioId is required");
        }
    }
}
