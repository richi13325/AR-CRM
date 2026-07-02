package com.ar.crm2.application.ai.port.in;

import com.ar.crm2.application.ai.command.RechazarAccionCommand;
import com.ar.crm2.model.entity.ia.AiAccion;

/**
 * Inbound use case: reject a pending AI action proposal. Side-effect
 * free at the application boundary — no CRM entity is touched.
 */
public interface RechazarAccionUseCase {

    AiAccion rechazar(RechazarAccionCommand command);
}