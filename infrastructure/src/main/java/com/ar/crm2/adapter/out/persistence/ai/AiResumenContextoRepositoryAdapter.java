package com.ar.crm2.adapter.out.persistence.ai;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiResumenContextoJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.mapper.AiResumenContextoMapper;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiResumenContextoSpringDataRepository;
import com.ar.crm2.application.ai.port.out.FindAiResumenPort;
import com.ar.crm2.application.ai.port.out.SaveAiResumenPort;
import com.ar.crm2.model.entity.ia.AiResumenContexto;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter for the AI context summary aggregate.
 *
 * <p>Implements the granular application ports {@link SaveAiResumenPort}
 * and {@link FindAiResumenPort}. The "latest summary" lookup is the
 * primary read path; the application services decide whether to
 * supersede the current summary based on the source watermark.
 */
@RequiredArgsConstructor
public class AiResumenContextoRepositoryAdapter
        implements SaveAiResumenPort, FindAiResumenPort {

    private final AiResumenContextoSpringDataRepository repository;

    // ── SaveAiResumenPort ──────────────────────────────────────────

    @Override
    public AiResumenContexto save(AiResumenContexto resumen) {
        AiResumenContextoJpaEntity entity = AiResumenContextoMapper.toEntity(resumen);
        AiResumenContextoJpaEntity saved = repository.save(entity);
        return AiResumenContextoMapper.toDomain(saved);
    }

    // ── FindAiResumenPort ──────────────────────────────────────────

    @Override
    public Optional<AiResumenContexto> findByConversacionId(UUID aiConversacionId) {
        return repository.findFirstByAiConversacionIdOrderByActualizadoEnDesc(aiConversacionId.toString())
            .map(AiResumenContextoMapper::toDomain);
    }
}
