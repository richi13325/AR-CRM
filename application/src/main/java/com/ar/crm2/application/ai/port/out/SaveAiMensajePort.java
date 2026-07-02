package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.ia.AiMensaje;

/**
 * Outbound port for persisting an AI message.
 */
public interface SaveAiMensajePort {

    AiMensaje save(AiMensaje mensaje);
}