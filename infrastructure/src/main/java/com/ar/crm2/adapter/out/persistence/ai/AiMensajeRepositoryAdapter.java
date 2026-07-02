package com.ar.crm2.adapter.out.persistence.ai;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiMensajeJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.mapper.AiMensajeMapper;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiMensajeSpringDataRepository;
import com.ar.crm2.application.ai.port.out.FindAiMensajesByConversacionPort;
import com.ar.crm2.application.ai.port.out.SaveAiMensajePort;
import com.ar.crm2.model.entity.ia.AiMensaje;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Persistence adapter for the AI message aggregate.
 *
 * <p>Implements the granular application ports
 * {@link SaveAiMensajePort} and {@link FindAiMensajesByConversacionPort}.
 * Messages are stored once and read in timestamp-ascending order so
 * the chat analyzer can rebuild the transcript for follow-up turns.
 */
@RequiredArgsConstructor
public class AiMensajeRepositoryAdapter
        implements SaveAiMensajePort, FindAiMensajesByConversacionPort {

    private final AiMensajeSpringDataRepository repository;

    // ── SaveAiMensajePort ──────────────────────────────────────────

    @Override
    public AiMensaje save(AiMensaje mensaje) {
        AiMensajeJpaEntity entity = AiMensajeMapper.toEntity(mensaje);
        AiMensajeJpaEntity saved = repository.save(entity);
        return AiMensajeMapper.toDomain(saved);
    }

    // ── FindAiMensajesByConversacionPort ───────────────────────────

    @Override
    public List<AiMensaje> findByConversacionId(UUID aiConversacionId) {
        return repository.findByAiConversacionIdOrderByCreadoEnAsc(aiConversacionId.toString()).stream()
            .map(AiMensajeMapper::toDomain)
            .toList();
    }
}
