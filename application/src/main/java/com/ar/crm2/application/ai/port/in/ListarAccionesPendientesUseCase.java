package com.ar.crm2.application.ai.port.in;

import com.ar.crm2.application.ai.command.ListarAccionesPendientesCommand;
import com.ar.crm2.model.entity.ia.AiAccion;

import java.util.List;

/**
 * Inbound use case: list the requester's PENDING AI action
 * proposals.
 *
 * <p>Implementation is responsible for tenant resolution (via the
 * Empresa-owned {@code ActorEmpresaScopePort}) and PENDING-only
 * filtering — the REST endpoint {@code GET /api/ai/acciones} is the
 * "my pending actions" inbox and MUST NOT surface REJECTED /
 * CONFIRMED / EXPIRED / EXECUTED / FAILED proposals.
 */
public interface ListarAccionesPendientesUseCase {

    List<AiAccion> listar(ListarAccionesPendientesCommand command);
}
