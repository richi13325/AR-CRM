package com.ar.crm2.whatsapp.application.mensaje.command;

import java.util.UUID;

public record ResponderBotCommand(UUID conversacionId, String contenido, boolean privado) {
    public ResponderBotCommand {
        if (conversacionId == null) throw new IllegalArgumentException("conversacionId es requerido");
        if (contenido == null || contenido.isBlank()) throw new IllegalArgumentException("contenido es requerido");
    }
}
