package com.ar.crm2.whatsapp.application.mensaje.command;

import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;

import java.util.UUID;

public record SendMensajeCommand(
        UUID conversacionId,
        UUID enviadoPor,
        TipoMensaje tipo,
        String contenido,
        String mediaUrl,
        boolean interna
) {
    public SendMensajeCommand {
        if (conversacionId == null) throw new IllegalArgumentException("conversacionId es requerido");
        if (enviadoPor == null) throw new IllegalArgumentException("enviadoPor es requerido");
        if (tipo == null) throw new IllegalArgumentException("tipo es requerido");
        boolean sinTexto = contenido == null || contenido.isBlank();
        boolean sinMedia = mediaUrl == null || mediaUrl.isBlank();
        if (sinTexto && sinMedia) throw new IllegalArgumentException("Se requiere texto o un adjunto");
    }
}
