package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.ia.AiMensaje;

import java.util.List;
import java.util.UUID;

/**
 * Outbound port for loading AI messages of one AI conversation.
 */
public interface FindAiMensajesByConversacionPort {

    List<AiMensaje> findByConversacionId(UUID aiConversacionId);
}