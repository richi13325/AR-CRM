package com.ar.crm2.application.ai.exception;

import com.ar.crm2.exception.TenantScopeViolationException;

import java.util.UUID;

/**
 * Thrown when an AI assistant request violates tenant ownership:
 * the actor does not own the company that owns the WhatsApp channel
 * or the AI conversation they want to operate on.
 *
 * <p>Maps to HTTP 403 Forbidden at the REST boundary.
 */
public class AsistenteTenantException extends RuntimeException {

    private AsistenteTenantException(String message) {
        super(message);
    }

    public static AsistenteTenantException chatNoPerteneceAlActor(String actorUsuarioId, String waConversacionId) {
        return new AsistenteTenantException(
                "El chat " + waConversacionId + " no pertenece a la empresa del usuario " + actorUsuarioId + "."
        );
    }

    public static AsistenteTenantException empresaNoEncontradaParaActor(String actorUsuarioId) {
        return new AsistenteTenantException(
                "El usuario " + actorUsuarioId + " no posee ninguna empresa."
        );
    }

    /**
     * PR6 cross-check rejection (resource-first tenant model):
     * the request supplies an {@code empresaId} that differs from the
     * addressed {@code AiAccion} resource's stored {@code empresaId}.
     *
     * <p>Distinct from {@link #from(TenantScopeViolationException, UUID, UUID)}
     * because the message describes a contract violation ("the action
     * does not belong to the selected company") rather than an authorization
     * failure ("you do not own this tenant"). Both map to HTTP 403 but the
     * caller-facing message and audit log differ.
     */
    public static AsistenteTenantException accionNoPerteneceALaEmpresaSeleccionada(
            String accionId, String empresaId
    ) {
        return new AsistenteTenantException(
                "La accion " + accionId + " no pertenece a la empresa seleccionada " + empresaId + "."
        );
    }

    /**
     * PR6 cross-check rejection (resource-first tenant model) for the AI
     * conversation endpoint — mirrors
     * {@link #accionNoPerteneceALaEmpresaSeleccionada(String, String)} with the
     * "This conversation does not belong to the selected company" wording.
     */
    public static AsistenteTenantException conversacionNoPerteneceALaEmpresaSeleccionada(
            String aiConversacionId, String empresaId
    ) {
        return new AsistenteTenantException(
                "La conversacion " + aiConversacionId + " no pertenece a la empresa seleccionada " + empresaId + "."
        );
    }

    /**
     * PR7 selector rejection: the actor does not own the {@code empresaId}
     * supplied on the request. Distinct from the resource-first cross-check
     * factories because this endpoint has no addressed resource — the
     * tenant IS the request parameter.
     *
     * <p>Used by {@code ListarAccionesPendientesService} when the
     * {@code ActorEmpresaScopePort} rejects the supplied {@code empresaId}
     * (or the actor owns no companies).
     */
    public static AsistenteTenantException empresaNoPoseidaPorActor(
            String actorUsuarioId, String empresaId
    ) {
        return new AsistenteTenantException(
                "La empresa " + empresaId + " no pertenece al usuario " + actorUsuarioId + "."
        );
    }

    /**
     * PR7 tenant selection rejection: the actor supplied a tenant via the
     * selector but does not own it. Same controlled shape as
     * {@link #empresaNoPoseidaPorActor(String, String)} but uses the
     * "selector" wording so the audit log distinguishes a missing
     * selector from a non-owned selector.
     */
    public static AsistenteTenantException tenantSelectorRechazado(
            String actorUsuarioId, String empresaId
    ) {
        return new AsistenteTenantException(
                "El selector de empresa " + empresaId
                        + " no pertenece al usuario " + actorUsuarioId + "."
        );
    }

    /**
     * Wraps a domain {@link TenantScopeViolationException} raised by
     * {@code EmpresaPermitidaPolicy}. Preserves the existing application
     * exception type visible to REST callers; the underlying domain
     * message is preserved in the exception cause chain.
     */
    public static AsistenteTenantException from(
            TenantScopeViolationException domain, UUID actorUsuarioId, UUID requestedEmpresaId
    ) {
        AsistenteTenantException app;
        if (requestedEmpresaId != null) {
            app = chatNoPerteneceAlActor(actorUsuarioId.toString(), requestedEmpresaId.toString());
        } else {
            app = empresaNoEncontradaParaActor(actorUsuarioId.toString());
        }
        app.initCause(domain);
        return app;
    }
}
