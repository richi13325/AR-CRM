package com.ar.crm2.application.ai.port.out.projection;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Application-owned projection of a WhatsApp message. Used by the
 * AI chat analysis use case to load transcripts without importing
 * the {@code whatsapp} module.
 *
 * <p>Lives in {@code port.out.projection} because it is the
 * application-owned projection that the {@code WhatsappMensajeLecturaPort}
 * returns to its callers.
 */
public record WhatsappMensajeResumen(
    UUID waMensajeId,
    UUID waConversacionId,
    String direccion,    // ENTRANTE | SALIENTE
    String tipo,         // TEXT | IMAGE | ...
    String contenido,
    String mediaUrl,
    LocalDateTime creadoEn
) {

    public WhatsappMensajeResumen {
        if (waMensajeId == null) {
            throw new IllegalArgumentException("waMensajeId is required");
        }
        if (waConversacionId == null) {
            throw new IllegalArgumentException("waConversacionId is required");
        }
        if (direccion == null || direccion.isBlank()) {
            throw new IllegalArgumentException("direccion is required");
        }
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("tipo is required");
        }
    }
}
