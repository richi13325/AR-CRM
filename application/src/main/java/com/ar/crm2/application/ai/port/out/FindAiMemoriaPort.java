package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.ia.AiMemoria;

import java.util.List;
import java.util.UUID;

/**
 * Outbound port for loading active AI memory records scoped to one AI
 * conversation (or one contact when the memory is contact-scoped).
 */
public interface FindAiMemoriaPort {

    List<AiMemoria> findActivasByConversacionId(UUID waConversacionId, UUID actorUsuarioId, UUID empresaId);
}