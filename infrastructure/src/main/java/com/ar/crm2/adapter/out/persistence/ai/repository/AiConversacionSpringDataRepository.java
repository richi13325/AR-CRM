package com.ar.crm2.adapter.out.persistence.ai.repository;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiConversacionJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for AI conversation persistence.
 *
 * <p>Derived queries per design.md §5:
 * <ul>
 *   <li>{@code findByActorUsuarioIdAndEmpresaIdOrderByActualizadoEnDesc(actor, empresa)} — the
 *       scope filter for {@code ListAiConversacionesPort}. Returns conversations
 *       visible to the requester inside their tenant, freshest first.</li>
 *   <li>{@code findByActorUsuarioIdAndWaConversacionId(actor, waConvId)} — the
 *       scope filter for the chat analyzer to detect an existing AI
 *       conversation for a given WhatsApp chat owned by the actor.</li>
 * </ul>
 */
@Repository
public interface AiConversacionSpringDataRepository extends JpaRepository<AiConversacionJpaEntity, String> {

    List<AiConversacionJpaEntity> findByActorUsuarioIdAndEmpresaIdOrderByActualizadoEnDesc(
        String actorUsuarioId, String empresaId, Pageable pageable
    );

    List<AiConversacionJpaEntity> findByActorUsuarioIdAndWaConversacionId(
        String actorUsuarioId, String waConversacionId
    );
}
