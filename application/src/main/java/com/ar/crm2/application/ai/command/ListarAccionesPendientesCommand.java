package com.ar.crm2.application.ai.command;

import java.util.UUID;

/**
 * Command to list the requester's PENDING AI action proposals.
 *
 * <p><b>User-approved PR7 contract:</b> {@code empresaId} is REQUIRED
 * at the command boundary. The system MUST NOT guess the tenant, MUST
 * NOT auto-resolve single-company actors, and MUST NOT fall back to
 * the first owned company when the caller omits the selector. The
 * REST DTO {@code ListarAccionesPendientesRequest} enforces the same
 * {@code @NotNull} invariant at the controller boundary so that the
 * service never sees a null {@code empresaId} under normal flow.
 *
 * <p>Authorization still happens in the application service: when a
 * non-null {@code empresaId} is supplied but the actor does not own
 * it, the service raises {@code AsistenteTenantException} (mapped to
 * 403) via the {@code ActorEmpresaScopePort} + translator pipeline.
 *
 * @param actorUsuarioId required
 * @param empresaId      required; the explicit tenant selector
 * @param limite         max proposals to return (1..200)
 */
public record ListarAccionesPendientesCommand(
    UUID actorUsuarioId,
    UUID empresaId,
    int limite
) {

    public ListarAccionesPendientesCommand {
        if (actorUsuarioId == null) {
            throw new IllegalArgumentException("actorUsuarioId is required");
        }
        if (empresaId == null) {
            throw new IllegalArgumentException("empresaId is required");
        }
        if (limite <= 0 || limite > 200) {
            throw new IllegalArgumentException("limite must be between 1 and 200");
        }
    }
}
