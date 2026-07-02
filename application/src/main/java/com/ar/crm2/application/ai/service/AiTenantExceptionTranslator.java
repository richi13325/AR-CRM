package com.ar.crm2.application.ai.service;

import com.ar.crm2.application.ai.exception.AsistenteTenantException;
import com.ar.crm2.application.empresa.port.in.ActorEmpresaScopePort;
import com.ar.crm2.exception.TenantScopeViolationException;
import com.ar.crm2.model.vo.EmpresaId;

import java.util.UUID;

/**
 * Small package-private helper that translates the neutral domain
 * {@link TenantScopeViolationException} raised by the Empresa-owned
 * {@link ActorEmpresaScopePort} into the AI-public
 * {@link AsistenteTenantException}.
 *
 * <p>Why a per-module helper (and not a thin resolver service): the
 * translation is a one-liner that any AI service can apply at its call
 * site. Introducing a separate resolver service
 * just to delegate to the Empresa port and re-throw is unnecessary
 * indirection — the Empresa port already exists and is the right
 * place to depend on. Each AI service that needs the AI-public
 * exception type wraps its {@code port.resolver(...)} call with
 * {@link #resolve(ActorEmpresaScopePort, UUID, UUID)}.
 *
 * <p>The helper lives next to the AI services that use it and is not
 * exposed to other bounded contexts. Consumers that do NOT need the
 * AI-public exception translation (e.g. {@code AnalizarChatService},
 * which only needs {@code empresasDelActor(...)}) call the port
 * directly without going through this helper.
 */
final class AiTenantExceptionTranslator {

    private AiTenantExceptionTranslator() {
    }

    /**
     * Resolves the tenant via the supplied port, translating the
     * neutral domain exception into the AI-public one.
     *
     * @param port           the Empresa-owned port implementation
     * @param actorUsuarioId the requester user id (required)
     * @param empresaId      optional explicit company id
     * @return the resolved company id
     * @throws AsistenteTenantException when the actor does not own
     *         the supplied {@code empresaId} or owns no companies
     */
    static EmpresaId resolve(ActorEmpresaScopePort port, UUID actorUsuarioId, UUID empresaId) {
        try {
            return port.resolver(actorUsuarioId, empresaId);
        } catch (TenantScopeViolationException ex) {
            throw AsistenteTenantException.from(ex, actorUsuarioId, empresaId);
        }
    }

    /**
     * PR7 selector resolution: same trust-boundary as
     * {@link #resolve(ActorEmpresaScopePort, UUID, UUID)} but with a
     * selector-specific message so callers / audit logs can distinguish
     * a non-owned tenant selector from a chat / resource tenant
     * mismatch.
     *
     * <p>Used by {@code ListarAccionesPendientesService} for the
     * {@code GET /api/ai/acciones} selector flow. The {@code empresaId}
     * is REQUIRED at the command boundary (the controller DTO +
     * canonical constructor both enforce non-null), so this helper
     * expects a non-null value.
     *
     * @param port           the Empresa-owned port implementation
     * @param actorUsuarioId the requester user id (required)
     * @param empresaId      the explicit tenant selector (required)
     * @return the resolved company id
     * @throws AsistenteTenantException when the actor does not own
     *         the supplied {@code empresaId}
     */
    static EmpresaId resolveForSelector(ActorEmpresaScopePort port, UUID actorUsuarioId, UUID empresaId) {
        try {
            return port.resolver(actorUsuarioId, empresaId);
        } catch (TenantScopeViolationException ex) {
            AsistenteTenantException app = AsistenteTenantException.tenantSelectorRechazado(
                    actorUsuarioId.toString(), empresaId.toString()
            );
            app.initCause(ex);
            throw app;
        }
    }

    /**
     * PR6 ownership validation: asserts the actor owns the supplied
     * resource tenant via the Empresa-owned port, translating the
     * neutral domain exception into the AI-public one.
     *
     * <p>Used by resource-bound AI services AFTER they load the target
     * resource and derive {@code recursoEmpresaId} from it. The actor
     * MUST own the resource tenant for authorization to succeed; the
     * port's {@link ActorEmpresaScopePort#resolver(UUID, UUID)} throws
     * {@link TenantScopeViolationException} when the actor owns a
     * different set of companies than the one requested.
     *
     * @param port             the Empresa-owned port implementation
     * @param actorUsuarioId   the requester user id (required)
     * @param recursoEmpresaId the tenant derived from the loaded resource (required)
     * @throws AsistenteTenantException when the actor does not own
     *         {@code recursoEmpresaId} or owns no companies
     */
    static void assertActorOwnsTenant(
            ActorEmpresaScopePort port, UUID actorUsuarioId, EmpresaId recursoEmpresaId
    ) {
        try {
            port.resolver(actorUsuarioId, recursoEmpresaId.value());
        } catch (TenantScopeViolationException ex) {
            throw AsistenteTenantException.from(ex, actorUsuarioId, recursoEmpresaId.value());
        }
    }
}
