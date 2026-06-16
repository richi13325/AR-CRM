package com.ar.crm2.whatsapp.application.conversacion.command;

import java.util.UUID;

public record AsignarAgenteCommand(UUID conversacionId, UUID usuarioId) {
    public AsignarAgenteCommand {
        if (conversacionId == null) throw new IllegalArgumentException("conversacionId es requerido");
        if (usuarioId == null) throw new IllegalArgumentException("usuarioId es requerido");
    }
}
