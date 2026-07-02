package com.ar.crm2.application.empresa.service;

import com.ar.crm2.application.empresa.port.in.ActorEmpresaScopePort;
import com.ar.crm2.application.empresa.port.out.FindEmpresasByCreadorPort;
import com.ar.crm2.exception.TenantScopeViolationException;
import com.ar.crm2.model.policy.EmpresaPermitidaPolicy;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Empresa-owned application coordinator that resolves the tenant
 * scope for a requester.
 *
 * <p>This service implements {@link ActorEmpresaScopePort} so any
 * bounded context (AI assistant, WhatsApp, contact, etc.) can depend
 * on the <b>port</b> rather than directly on this class. The port
 * boundary keeps application services from coupling to a different
 * bounded context's service implementation.
 *
 * <p>The service is intentionally neutral about the consuming module:
 * any module that needs to map a requester to the companies they own
 * can reuse it. The AI assistant is one consumer but not the only
 * intended one, so the service does NOT depend on any
 * {@code application.ai.*} type. The neutral domain exception
 * {@link TenantScopeViolationException} propagates as-is; callers
 * that need a different public exception type (e.g. an AI-specific
 * one) translate it at their own boundary.
 *
 * <p>The class is a thin coordinator: it fetches the list of owned
 * companies via {@link FindEmpresasByCreadorPort} and delegates the
 * actual policy decision to {@link EmpresaPermitidaPolicy} (domain).
 *
 * <p>Tenant ownership is derived from
 * {@code Empresa.creadoPor == ActorContext.usuarioId}. When the
 * caller supplies an explicit {@code empresaId} the resolver verifies
 * that the actor owns it; otherwise it returns the FIRST owned
 * company id (the current single-company-per-user fallback).
 */
@RequiredArgsConstructor
public class ActorEmpresaScopeService implements ActorEmpresaScopePort {

    private final FindEmpresasByCreadorPort findEmpresasByCreadorPort;

    /**
     * Resolves the tenant scope for the supplied actor.
     *
     * @param actorUsuarioId the requester user id (required)
     * @param empresaId      optional explicit company id
     * @return the resolved company id
     * @throws TenantScopeViolationException when the actor does not
     *         own the supplied {@code empresaId} or owns no companies
     *         at all. Callers (e.g. the AI module) translate this
     *         neutral domain exception into their own public exception
     *         type at the application boundary.
     */
    @Override
    public EmpresaId resolver(UUID actorUsuarioId, UUID empresaId) {
        if (actorUsuarioId == null) {
            throw new IllegalArgumentException("actorUsuarioId is required");
        }
        List<EmpresaId> owned = findEmpresasByCreadorPort.findEmpresasByCreador(
                UsuarioId.from(actorUsuarioId)
        );
        return EmpresaPermitidaPolicy.seleccionarEmpresaPermitida(owned, empresaId);
    }

    /**
     * Returns the list of company ids owned by the supplied actor.
     * Used by services that need the full tenant scope (e.g. multi-
     * empresa cross-checks).
     */
    @Override
    public List<EmpresaId> empresasDelActor(UUID actorUsuarioId) {
        if (actorUsuarioId == null) {
            throw new IllegalArgumentException("actorUsuarioId is required");
        }
        return findEmpresasByCreadorPort.findEmpresasByCreador(UsuarioId.from(actorUsuarioId));
    }
}