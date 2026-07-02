package com.ar.crm2.adapter.out.persistence.ai;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiConversacionJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.mapper.AiConversacionMapper;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiConversacionSpringDataRepository;
import com.ar.crm2.application.ai.port.out.FindAiConversacionPort;
import com.ar.crm2.application.ai.port.out.ListAiConversacionesPort;
import com.ar.crm2.model.entity.ia.AiConversacion;
import com.ar.crm2.model.vo.AiConversacionId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter for the AI conversation aggregate — read side.
 *
 * <p>Implements {@link FindAiConversacionPort} and
 * {@link ListAiConversacionesPort}. The write-side port
 * {@link com.ar.crm2.application.ai.port.out.SaveAiConversacionPort}
 * is implemented by {@link AiConversacionSaveAdapter} because the two
 * ports declare {@code findById(UUID)} with different return types
 * ({@code AiConversacion} vs {@code Optional<AiConversacion>}) and
 * Java does not allow overloading by return type.
 */
@RequiredArgsConstructor
public class AiConversacionRepositoryAdapter
        implements FindAiConversacionPort, ListAiConversacionesPort {

    private final AiConversacionSpringDataRepository repository;

    // ── FindAiConversacionPort ─────────────────────────────────────

    @Override
    public Optional<AiConversacion> findById(UUID id) {
        return repository.findById(id.toString())
            .map(AiConversacionMapper::toDomain);
    }

    /**
     * Convenience overload for callers carrying an {@link AiConversacionId}.
     */
    public Optional<AiConversacion> findById(AiConversacionId id) {
        return findById(id.value());
    }

    // ── ListAiConversacionesPort ───────────────────────────────────

    @Override
    public List<AiConversacion> listByActor(UUID actorUsuarioId, UUID empresaId, int limite) {
        Pageable pageable = PageRequest.of(0, Math.max(1, limite));
        return repository.findByActorUsuarioIdAndEmpresaIdOrderByActualizadoEnDesc(
                actorUsuarioId.toString(),
                empresaId.toString(),
                pageable
            ).stream()
            .map(AiConversacionMapper::toDomain)
            .toList();
    }
}
