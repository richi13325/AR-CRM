package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.ia.AiAccion;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for finding an AI action proposal by id.
 */
public interface FindAiAccionPort {

    Optional<AiAccion> findById(UUID id);
}