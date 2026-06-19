package com.ar.crm2.whatsapp.application.mensaje.command;

import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReceiveMensajeCommand(
        UUID canalId,
        String waMessageId,
        String numeroTelefono,
        String nombreContacto,
        TipoMensaje tipo,
        String contenido,
        String mediaUrl,
        Object rawMensaje,
        boolean esSaliente,
        LocalDateTime timestamp
) {
    public ReceiveMensajeCommand {
        if (canalId == null) throw new IllegalArgumentException("canalId es requerido");
        if (waMessageId == null || waMessageId.isBlank()) throw new IllegalArgumentException("waMessageId es requerido");
        if (numeroTelefono == null || numeroTelefono.isBlank()) throw new IllegalArgumentException("numeroTelefono es requerido");
        if (tipo == null) throw new IllegalArgumentException("tipo es requerido");
    }
}
