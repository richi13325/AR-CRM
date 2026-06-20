package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.whatsapp.domain.enums.TipoMensaje;
import jakarta.validation.constraints.NotNull;

public record SendMensajeWaRequest(
        @NotNull TipoMensaje tipo,
        String contenido,
        String mediaUrl,
        boolean interna
) {
    // contenido puede venir vacío cuando se manda solo un adjunto (imagen sin caption),
    // pero al menos uno de los dos (texto o media) debe estar presente.
    public SendMensajeWaRequest {
        boolean sinTexto = contenido == null || contenido.isBlank();
        boolean sinMedia = mediaUrl == null || mediaUrl.isBlank();
        if (sinTexto && sinMedia) {
            throw new IllegalArgumentException("El mensaje necesita texto o un adjunto");
        }
    }
}
