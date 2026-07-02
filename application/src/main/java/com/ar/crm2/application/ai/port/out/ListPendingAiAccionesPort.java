package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.ia.AiAccion;

import java.util.List;
import java.util.UUID;

/**
 * Outbound port for listing PENDING AI action proposals for an
 * actor within a tenant.
 *
 * <p>Implementation in infrastructure uses
 * {@code findBySolicitadaPorAndEstado} with
 * {@code estado = PENDING} and a pageable cap.
 */
public interface ListPendingAiAccionesPort {

    List<AiAccion> listPendingByActor(UUID actorUsuarioId, UUID empresaId, int limite);
}
