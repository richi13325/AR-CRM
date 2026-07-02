package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.ia.AiConversacion;

import java.util.UUID;

/**
 * Outbound port for persisting an AI conversation (save + update).
 */
public interface SaveAiConversacionPort {

    AiConversacion save(AiConversacion conversacion);

    AiConversacion findById(UUID id);
}