package com.ar.crm2.adapter.out.persistence.ai.repository;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiResumenContextoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for AI context summary persistence.
 *
 * <p>Derived query per design.md §5: the most recent summary is looked
 * up by AI conversation id; the application layer decides whether
 * it must be regenerated based on the source watermark.
 */
@Repository
public interface AiResumenContextoSpringDataRepository extends JpaRepository<AiResumenContextoJpaEntity, String> {

    Optional<AiResumenContextoJpaEntity> findFirstByAiConversacionIdOrderByActualizadoEnDesc(String aiConversacionId);
}
