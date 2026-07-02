package com.ar.crm2.adapter.in.rest.dto.ai;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Inbound REST request for {@code GET /api/ai/acciones} (PR7 selector).
 *
 * <p><b>User-approved PR7 contract:</b> {@code empresaId} is REQUIRED at
 * the REST boundary. The system MUST NOT guess, MUST NOT auto-resolve
 * single-tenant actors, and MUST NOT fall back to the first owned
 * company when the caller omits the selector. The validation belongs
 * here, in the DTO, not in the controller and not in the application
 * service.
 *
 * <p>The constraint annotations below are the REST boundary enforcement
 * of that contract. The application command and the canonical
 * constructor enforce the same non-null invariant as defense in depth.
 *
 * <p>The {@code limite} bounds (1..200) match the application command's
 * canonical constructor validator. Leaving {@code limite} blank is
 * permitted; the controller applies a default of {@code 50} so a missing
 * value behaves like an explicit "give me the inbox page" request.
 *
 * @param empresaId required tenant selector (UUID)
 * @param limite    optional page size (1..200); nullable means "use default"
 */
public record ListarAccionesPendientesRequest(
    @NotNull(message = "empresaId is required") UUID empresaId,
    @Min(value = 1, message = "limite must be >= 1")
    @Max(value = 200, message = "limite must be <= 200")
    Integer limite
) {
}
