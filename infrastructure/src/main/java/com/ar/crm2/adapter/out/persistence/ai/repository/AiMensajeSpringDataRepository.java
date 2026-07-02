package com.ar.crm2.adapter.out.persistence.ai.repository;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiMensajeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for AI message persistence.
 *
 * <p>Derived query per design.md §5:
 * <ul>
 *   <li>{@code findByAiConversacionIdOrderByCreadoEnAsc} — the ordered transcript
 *       loaded by {@code FindAiMensajesByConversacionPort}.</li>
 * </ul>
 */
@Repository
public interface AiMensajeSpringDataRepository extends JpaRepository<AiMensajeJpaEntity, String> {

    List<AiMensajeJpaEntity> findByAiConversacionIdOrderByCreadoEnAsc(String aiConversacionId);
}
