package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.ia.AiConversacion;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for finding an AI conversation by id.
 */
public interface FindAiConversacionPort {

    Optional<AiConversacion> findById(UUID id);
}