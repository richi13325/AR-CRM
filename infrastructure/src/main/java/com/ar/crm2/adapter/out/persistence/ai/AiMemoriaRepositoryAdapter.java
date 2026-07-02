package com.ar.crm2.adapter.out.persistence.ai;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiMemoriaJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.mapper.AiMemoriaMapper;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiMemoriaSpringDataRepository;
import com.ar.crm2.application.ai.port.out.DeleteAiMemoriaPort;
import com.ar.crm2.application.ai.port.out.FindAiMemoriaPort;
import com.ar.crm2.application.ai.port.out.SaveAiMemoriaPort;
import com.ar.crm2.model.entity.ia.AiMemoria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Persistence adapter for the AI memory aggregate.
 *
 * <p>Implements the granular application ports
 * {@link SaveAiMemoriaPort}, {@link FindAiMemoriaPort} and
 * {@link DeleteAiMemoriaPort}. Memory is always private to a
 * requester + company + (waConversacionId | contactoId) scope; the
 * adapter never exposes cross-scope data.
 */
public class AiMemoriaRepositoryAdapter
        implements SaveAiMemoriaPort, FindAiMemoriaPort, DeleteAiMemoriaPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiMemoriaRepositoryAdapter.class);

    private final AiMemoriaSpringDataRepository repository;
    private final boolean memoryWritesEnabled;

    public AiMemoriaRepositoryAdapter(
            AiMemoriaSpringDataRepository repository,
            @Value("${ai-assistant.phase1.memory-writes-enabled:false}") boolean memoryWritesEnabled
    ) {
        this.repository = repository;
        this.memoryWritesEnabled = memoryWritesEnabled;
    }

    // ── SaveAiMemoriaPort ──────────────────────────────────────────

    @Override
    public AiMemoria save(AiMemoria memoria) {
        assertMemoryWritesEnabled("save");
        AiMemoriaJpaEntity entity = AiMemoriaMapper.toEntity(memoria);
        AiMemoriaJpaEntity saved = repository.save(entity);
        return AiMemoriaMapper.toDomain(saved);
    }

    // ── FindAiMemoriaPort ──────────────────────────────────────────

    @Override
    public List<AiMemoria> findActivasByConversacionId(
        UUID waConversacionId, UUID actorUsuarioId, UUID empresaId
    ) {
        return repository
            .findActiveMemories(
                actorUsuarioId.toString(),
                empresaId.toString(),
                waConversacionId.toString(),
                LocalDateTime.now()
            ).stream()
            .map(AiMemoriaMapper::toDomain)
            .toList();
    }

    // ── DeleteAiMemoriaPort ────────────────────────────────────────

    @Override
    public void delete(AiMemoria memoria) {
        assertMemoryWritesEnabled("delete");
        repository.deleteById(memoria.getId().value().toString());
    }

    private void assertMemoryWritesEnabled(String operation) {
        if (memoryWritesEnabled) {
            return;
        }
        LOGGER.warn(
                "Rejected AI memory {} because ai-assistant.phase1.memory-writes-enabled=false.",
                operation
        );
        throw new IllegalStateException("AI memory writes are disabled in phase 1.");
    }
}
