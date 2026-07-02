package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.ia.AiResumenContexto;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for finding the current context summary for one AI
 * conversation.
 */
public interface FindAiResumenPort {

    Optional<AiResumenContexto> findByConversacionId(UUID aiConversacionId);
}