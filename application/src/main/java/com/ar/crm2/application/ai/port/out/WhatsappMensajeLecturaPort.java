package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.application.ai.port.out.projection.WhatsappMensajeResumen;

import java.util.List;
import java.util.UUID;

/**
 * Outbound port for reading WhatsApp messages scoped to a conversation.
 *
 * <p>Returns application-owned projections (no {@code whatsapp} import).
 */
public interface WhatsappMensajeLecturaPort {

    /**
     * Loads the messages of the supplied WhatsApp conversation ordered
     * by created timestamp ascending.
     */
    List<WhatsappMensajeResumen> findByConversacionId(UUID waConversacionId);
}