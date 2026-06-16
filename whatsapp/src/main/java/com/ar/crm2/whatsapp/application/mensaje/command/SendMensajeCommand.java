package com.ar.crm2.whatsapp.application.mensaje.command;

import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;

import java.util.UUID;

public record SendMensajeCommand(
        UUID conversacionId,
        UUID enviadoPor,
        TipoMensaje tipo,
        String contenido,
        String mediaUrl
) {
    public SendMensajeCommand {
        if (conversacionId == null) throw new IllegalArgumentException("conversacionId es requerido");
        if (enviadoPor == null) throw new IllegalArgumentException("enviadoPor es requerido");
        if (tipo == null) throw new IllegalArgumentException("tipo es requerido");
        if (contenido == null || contenido.isBlank()) throw new IllegalArgumentException("contenido es requerido");
    }
}
