package com.ar.crm2.adapter.out.persistence.ai.repository;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiAccionJpaEntity;
import com.ar.crm2.model.enums.EstadoAccion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for AI action proposal persistence.
 *
 * <p>Derived queries per design.md §5:
 * <ul>
 *   <li>{@code findByEstadoAndExpiresAtBefore(estado, cutoff, pageable)} —
 *       the expiry-sweep feed consumed by {@code UpdateEstadoAccionPort}
 *       to flip stale PENDING proposals to EXPIRED.</li>
 *   <li>{@code findBySolicitadaPorAndEstado(actor, estado, pageable)} —
 *       the requester-scope index lookup. Powers owner-only inbox
 *       views where the application has already established the
 *       tenant and the index is intentionally tenant-agnostic.</li>
 *   <li>{@code findBySolicitadaPorAndEstadoAndEmpresaId(actor, estado, empresaId, pageable)} —
 *       PR7 selector query. The {@code GET /api/ai/acciones} endpoint
 *       requires an explicit {@code empresaId} selector; the application
 *       service enforces ownership via {@code ActorEmpresaScopePort},
 *       and the SQL filter here is the trust-boundary guarantee that
 *       only that tenant's rows are returned.</li>
 * </ul>
 */
@Repository
public interface AiAccionSpringDataRepository extends JpaRepository<AiAccionJpaEntity, String> {

    List<AiAccionJpaEntity> findByEstadoAndExpiresAtBefore(
        EstadoAccion estado, LocalDateTime cutoff, Pageable pageable
    );

    List<AiAccionJpaEntity> findBySolicitadaPorAndEstado(
        String solicitadaPor, EstadoAccion estado, Pageable pageable
    );

    List<AiAccionJpaEntity> findBySolicitadaPorAndEstadoAndEmpresaId(
        String solicitadaPor, EstadoAccion estado, String empresaId, Pageable pageable
    );
}
