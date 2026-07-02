package com.ar.crm2.model.policy;

import com.ar.crm2.exception.TenantScopeViolationException;
import com.ar.crm2.model.vo.EmpresaId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Domain policy that selects the company an actor is allowed to use
 * for AI operations.
 *
 * <p>The rule:
 * <ol>
 *   <li>If an explicit {@code empresaId} is supplied, it MUST belong
 *       to the actor's owned set (validated via
 *       {@code FindEmpresasByCreadorPort}); otherwise the request is
 *       rejected as a tenant violation.</li>
 *   <li>If no explicit {@code empresaId} is supplied, the FIRST owned
 *       company is returned (the current single-company-per-user
 *       fallback). This fallback is intentional: phase-1 AI does
 *       NOT support multi-empresa tenants.</li>
 * </ol>
 *
 * <p>This class is pure: it does NOT touch any port. The application
 * resolver fetches the list of owned companies and delegates to this
 * policy, keeping the application layer a thin coordinator.
 *
 * <p>Throws {@link TenantScopeViolationException} on rejection — the
 * application resolver may catch and rethrow as the application-level
 * {@code AsistenteTenantException} to preserve the existing public
 * exception type visible to REST callers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmpresaPermitidaPolicy {

    /**
     * Selects the company the actor is allowed to use.
     *
     * @param owned       list of company ids the actor owns (already fetched)
     * @param requestedId optional explicit {@code empresaId}; nullable
     * @return the selected {@link EmpresaId}
     * @throws TenantScopeViolationException when the explicit id is not
     *                                       in the owned set or the
     *                                       actor owns no companies
     */
    public static EmpresaId seleccionarEmpresaPermitida(List<EmpresaId> owned, UUID requestedId) {
        if (requestedId != null) {
            EmpresaId requested = EmpresaId.from(requestedId);
            if (!owned.contains(requested)) {
                throw new TenantScopeViolationException(
                        "La empresa " + requestedId + " no pertenece al actor."
                );
            }
            return requested;
        }
        if (owned.isEmpty()) {
            throw new TenantScopeViolationException(
                    "El actor no posee ninguna empresa."
            );
        }
        return owned.get(0);
    }
}
