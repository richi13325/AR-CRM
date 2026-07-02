package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.ia.AiConversacion;

import java.util.List;
import java.util.UUID;

/**
 * Outbound port for listing AI conversations for a requester.
 *
 * <p>Implementation in infrastructure uses
 * {@code findByActorUsuarioIdOrderByActualizadoEnDesc}.
 */
public interface ListAiConversacionesPort {

    List<AiConversacion> listByActor(UUID actorUsuarioId, UUID empresaId, int limite);
}