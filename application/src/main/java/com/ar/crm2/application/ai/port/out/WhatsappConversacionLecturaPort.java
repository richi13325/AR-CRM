package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.application.ai.port.out.projection.WhatsappConversacionResumen;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for reading WhatsApp conversation summaries.
 *
 * <p>Application-owned projection (see {@link WhatsappConversacionResumen})
 * keeps the {@code application} module independent from {@code whatsapp}.
 */
public interface WhatsappConversacionLecturaPort {

    Optional<WhatsappConversacionResumen> findById(UUID waConversacionId);
}