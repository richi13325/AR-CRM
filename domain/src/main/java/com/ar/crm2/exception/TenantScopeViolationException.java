package com.ar.crm2.exception;

/**
 * Exception thrown when an actor's tenant scope cannot be resolved
 * against the supplied {@code empresaId}: the explicit company id is
 * not in the actor's owned set, or the actor owns no companies at all
 * and no explicit id was supplied.
 *
 * <p>Thrown by the domain policy
 * {@code com.ar.crm2.model.policy.EmpresaPermitidaPolicy}. The
 * application resolver may catch and rethrow it as
 * {@code AsistenteTenantException} to preserve the existing
 * application-level exception type visible to REST callers.
 *
 * <p>Maps to HTTP 403 Forbidden at the REST boundary.
 */
public class TenantScopeViolationException extends DomainException {

    public TenantScopeViolationException(String message) {
        super(message);
    }
}
