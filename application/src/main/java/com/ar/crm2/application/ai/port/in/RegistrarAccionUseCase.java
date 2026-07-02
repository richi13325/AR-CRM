package com.ar.crm2.application.ai.port.in;

import com.ar.crm2.application.ai.command.RegistrarAccionCommand;
import com.ar.crm2.model.entity.ia.AiAccion;

/**
 * Inbound input port for staging a new AI action proposal in PENDING state.
 *
 * <p>Single-method contract per project rules. The use case never invokes
 * real CRM mutation use cases; it only stages a proposal that the same
 * user must later confirm via {@code ConfirmarAccionUseCase}.
 */
public interface RegistrarAccionUseCase {

    /**
     * Stages a new AI action proposal.
     *
     * @param command the staging command with actor, scope, payload and ttl
     * @return the persisted AiAccion in PENDING state
     */
    AiAccion registrar(RegistrarAccionCommand command);
}