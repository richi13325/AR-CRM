package com.ar.crm2.application.ai.port.in;

import com.ar.crm2.application.ai.command.ObtenerAccionCommand;
import com.ar.crm2.model.entity.ia.AiAccion;

/**
 * Inbound use case: fetch a single AI action proposal by id, scoped
 * to the requester and their tenant.
 */
public interface ObtenerAccionUseCase {

    AiAccion obtener(ObtenerAccionCommand command);
}