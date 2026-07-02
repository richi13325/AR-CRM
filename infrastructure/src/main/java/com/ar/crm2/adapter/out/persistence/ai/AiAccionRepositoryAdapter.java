package com.ar.crm2.adapter.out.persistence.ai;

import com.ar.crm2.adapter.out.persistence.ai.entity.AiAccionJpaEntity;
import com.ar.crm2.adapter.out.persistence.ai.mapper.AiAccionMapper;
import com.ar.crm2.adapter.out.persistence.ai.repository.AiAccionSpringDataRepository;
import com.ar.crm2.application.ai.port.out.FindAiAccionPort;
import com.ar.crm2.application.ai.port.out.ListPendingAiAccionesPort;
import com.ar.crm2.application.ai.port.out.SaveAiAccionPort;
import com.ar.crm2.application.ai.port.out.UpdateEstadoAccionPort;
import com.ar.crm2.model.entity.ia.AiAccion;
import com.ar.crm2.model.enums.EstadoAccion;
import com.ar.crm2.model.vo.AiAccionId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter for the AI action proposal aggregate.
 *
 * <p>Implements the granular application ports
 * {@link SaveAiAccionPort}, {@link FindAiAccionPort},
 * {@link UpdateEstadoAccionPort} and {@link ListPendingAiAccionesPort}.
 * The expiry-sweep query {@code findByEstadoAndExpiresAtBefore}
 * feeds {@code ExpirarAccionService}; the requester-scope query
 * {@code findBySolicitadaPorAndEstado} powers the
 * {@code ListarAccionesPendientesUseCase} inbox endpoint.
 *
 * <p><b>Safety boundary:</b> the adapter never invokes CRM mutation
 * use cases — proposals are staged in PENDING state and only the
 * {@code ConfirmarAccionUseCase} may dispatch real mutations. The
 * adapter just stores and reads the proposal rows.
 */
@RequiredArgsConstructor
public class AiAccionRepositoryAdapter
        implements SaveAiAccionPort, FindAiAccionPort, UpdateEstadoAccionPort,
                   ListPendingAiAccionesPort {

    private final AiAccionSpringDataRepository repository;

    // ── SaveAiAccionPort ───────────────────────────────────────────

    @Override
    public AiAccion save(AiAccion accion) {
        AiAccionJpaEntity entity = AiAccionMapper.toEntity(accion);
        AiAccionJpaEntity saved = repository.save(entity);
        return AiAccionMapper.toDomain(saved);
    }

    // ── FindAiAccionPort ───────────────────────────────────────────

    @Override
    public Optional<AiAccion> findById(UUID id) {
        return repository.findById(id.toString()).map(AiAccionMapper::toDomain);
    }

    /**
     * Convenience overload for callers that already carry an
     * {@link AiAccionId} value object.
     */
    public Optional<AiAccion> findById(AiAccionId id) {
        return findById(id.value());
    }

    // ── UpdateEstadoAccionPort ─────────────────────────────────────

    @Override
    public List<AiAccion> findPendingExpired(int limite) {
        Pageable pageable = PageRequest.of(0, Math.max(1, limite));
        return repository
            .findByEstadoAndExpiresAtBefore(EstadoAccion.PENDING, LocalDateTime.now(), pageable)
            .stream()
            .map(AiAccionMapper::toDomain)
            .toList();
    }

    // ── ListPendingAiAccionesPort ──────────────────────────────────

    /**
     * Returns the {@code estado = PENDING} proposals staged by the
     * supplied actor within the supplied tenant, capped by the
     * supplied {@code limite}.
     *
     * <p><b>User-approved PR7 contract:</b> the
     * {@code (solicitadaPor, estado, empresaId)} triple filter is the
     * SQL trust boundary for the {@code GET /api/ai/acciones}
     * selector endpoint. The application service enforces ownership
     * via {@code ActorEmpresaScopePort}; this query guarantees that
     * ownership is not the only line of defense — even if the service
     * were bypassed, the SQL filter would still only return rows for
     * the supplied tenant.
     *
     * <p>Previously the inbox used {@code findBySolicitadaPorAndEstado}
     * (actor-only); that query is kept on the repository interface
     * for future owner-dashboard / cross-tenant audit views where the
     * tenant is established by other code paths, but the selector
     * endpoint MUST NOT call it.
     */
    @Override
    public List<AiAccion> listPendingByActor(UUID actorUsuarioId, UUID empresaId, int limite) {
        Pageable pageable = PageRequest.of(0, Math.max(1, limite));
        return repository
            .findBySolicitadaPorAndEstadoAndEmpresaId(
                actorUsuarioId.toString(),
                EstadoAccion.PENDING,
                empresaId.toString(),
                pageable
            )
            .stream()
            .map(AiAccionMapper::toDomain)
            .toList();
    }
}
