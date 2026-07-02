package com.ar.crm2.adapter.out.persistence.ai.repository;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiMemoriaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for AI memory persistence.
 *
 * <p>The "active memory by scope" query is implemented with explicit
 * JPQL via {@code @Query} and an intention-revealing method name. A
 * derived method name for this filter would be too long and would make
 * the repository contract harder to review.
 *
 * <p>The phase-1 conversation read contract is enforced here at the
 * SQL/JPQL boundary: only conversation-scoped rows are returned, even
 * if malformed legacy data carries a matching WhatsApp conversation id
 * under a different visibility.
 */
@Repository
public interface AiMemoriaSpringDataRepository extends JpaRepository<AiMemoriaJpaEntity, String> {

    /**
     * Returns the memory rows for a requester + company + WhatsApp
     * conversation that are NOT superseded, NOT explicitly expired,
     * whose {@code expiresAt} is still in the future, and whose
     * visibility is explicitly {@code CONVERSACION_SCOPED}. This is
     * the phase-1 conversation-scoped read surface; contact-scoped
     * memory retrieval remains deferred.
     */
    @Query("""
        SELECT m
        FROM AiMemoriaJpaEntity m
        WHERE m.actorUsuarioId = :actorUsuarioId
          AND m.empresaId = :empresaId
          AND m.waConversacionId = :waConversacionId
          AND m.visibilidad = com.ar.crm2.model.enums.VisibilidadMemoria.CONVERSACION_SCOPED
          AND m.superseded = false
          AND m.expirada = false
          AND m.expiresAt > :ahora
        """)
    List<AiMemoriaJpaEntity> findActiveMemories(
        @Param("actorUsuarioId") String actorUsuarioId,
        @Param("empresaId") String empresaId,
        @Param("waConversacionId") String waConversacionId,
        @Param("ahora") LocalDateTime ahora
    );
}
