package com.ar.crm2.adapter.out.persistence.ai;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiConversacionJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.mapper.AiConversacionMapper;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiConversacionSpringDataRepository;
import com.ar.crm2.application.ai.port.out.SaveAiConversacionPort;
import com.ar.crm2.model.entity.ia.AiConversacion;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Persistence adapter for the AI conversation aggregate — write side.
 *
 * <p>Implements {@link SaveAiConversacionPort} which exposes both
 * {@code save(...)} and {@code findById(UUID)} (returning the entity or
 * {@code null} when missing). The companion read-side adapter is
 * {@link AiConversacionRepositoryAdapter}.
 *
 * <p>The write side is intentionally split from the read side because
 * the two port interfaces declare {@code findById(UUID)} with
 * different return types ({@code AiConversacion} vs
 * {@code Optional<AiConversacion>}) — Java does not allow overloading
 * by return type, so a single class cannot implement both ports.
 */
@RequiredArgsConstructor
public class AiConversacionSaveAdapter implements SaveAiConversacionPort {

    private final AiConversacionSpringDataRepository repository;

    @Override
    public AiConversacion save(AiConversacion conversacion) {
        AiConversacionJpaEntity entity = AiConversacionMapper.toEntity(conversacion);
        AiConversacionJpaEntity saved = repository.save(entity);
        return AiConversacionMapper.toDomain(saved);
    }

    @Override
    public AiConversacion findById(UUID id) {
        return repository.findById(id.toString())
            .map(AiConversacionMapper::toDomain)
            .orElse(null);
    }
}
