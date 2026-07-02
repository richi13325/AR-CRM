package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.ia.AiAccion;

/**
 * Granular outbound port for persisting a new or updated AiAccion.
 *
 * <p>Single-method contract per project rules. Implementation lives in
 * infrastructure (PR 2) using a JPA repository adapter; for now the
 * application layer is framework-free.
 */
public interface SaveAiAccionPort {

    /**
     * Persists an AI action proposal.
     *
     * @param accion the domain entity to persist
     * @return the persisted entity (with id and timestamps populated)
     */
    AiAccion save(AiAccion accion);
}