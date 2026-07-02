package com.ar.crm2.application.empresa.port.in;

import com.ar.crm2.model.vo.EmpresaId;

import java.util.List;
import java.util.UUID;

/**
 * Empresa-owned inbound port for resolving the tenant scope of a
 * requester (actor).
 *
 * <p>This port is intentionally <b>neutral</b> about the consuming
 * bounded context: the AI assistant, the WhatsApp module, the contact
 * module, and any future tenant-bound context can depend on it without
 * importing {@code application.ai.*} types. The implementation lives in
 * the Empresa application service
 * {@code com.ar.crm2.application.empresa.service.ActorEmpresaScopeService},
 * which fetches the actor's owned companies through the granular outbound
 * port {@code FindEmpresasByCreadorPort} and delegates the policy decision
 * to {@code com.ar.crm2.model.policy.EmpresaPermitidaPolicy}.
 *
 * <p>The port contract deliberately raises the <b>neutral domain</b>
 * exception {@code com.ar.crm2.exception.TenantScopeViolationException}
 * on tenant rejection. Each consuming bounded context (e.g. the AI
 * module) translates that neutral exception to its own public exception
 * type at the application boundary, so the Empresa service stays
 * decoupled from any consumer-specific exception vocabulary.
 *
 * <p>Architectural rationale: a service should depend on a port, not on
 * another bounded context's service. The previous AI design introduced
 * a thin resolver port whose only job was to delegate to the Empresa
 * service and translate the neutral domain exception into the AI-public
 * exception type. That indirection was redundant —
 * the translation belongs at the AI call site, not at a separate
 * resolver layer.
 */
public interface ActorEmpresaScopePort {

    /**
     * Resolves the tenant scope for the supplied actor.
     *
     * @param actorUsuarioId the requester user id (required, non-null)
     * @param empresaId      optional explicit company id; when {@code null}
     *                       the resolver returns the actor's first owned
     *                       company (single-empresa fallback)
     * @return the resolved {@link EmpresaId}
     * @throws com.ar.crm2.exception.TenantScopeViolationException when the
     *         explicit {@code empresaId} is not owned by the actor, or
     *         the actor owns no companies at all. Callers that need a
     *         different public exception type translate this neutral
     *         domain exception at their own application boundary.
     */
    EmpresaId resolver(UUID actorUsuarioId, UUID empresaId);

    /**
     * Returns the list of company ids owned by the supplied actor.
     *
     * <p>Used by services that need the full tenant scope (e.g. for
     * cross-checks on resource ownership, where the resource carries a
     * tenant id and the service must verify the actor owns it).
     *
     * @param actorUsuarioId the requester user id (required, non-null)
     * @return the immutable list of owned {@link EmpresaId} values
     */
    List<EmpresaId> empresasDelActor(UUID actorUsuarioId);
}
