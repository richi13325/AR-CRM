package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import com.ar.crm2.whatsapp.domain.enums.DireccionMensaje;
import com.ar.crm2.whatsapp.domain.enums.StatusMensaje;
import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;

import java.time.LocalDateTime;
import java.util.UUID;

public record MensajeWaResponse(
        UUID id,
        UUID conversacionId,
        String waMessageId,
        TipoMensaje tipo,
        DireccionMensaje direccion,
        String contenido,
        String mediaUrl,
        StatusMensaje status,
        UUID enviadoPor,
        LocalDateTime creadoEn
) {
    public static MensajeWaResponse fromDomain(Mensaje m) {
        return new MensajeWaResponse(
                m.getId().value(),
                m.getConversacionId().value(),
                m.getWaMessageId(),
                m.getTipo(),
                m.getDireccion(),
                m.getContenido(),
                m.getMediaUrl(),
                m.getStatus(),
                m.getEnviadoPor() != null ? m.getEnviadoPor().value() : null,
                m.getCreadoEn()
        );
    }
}
