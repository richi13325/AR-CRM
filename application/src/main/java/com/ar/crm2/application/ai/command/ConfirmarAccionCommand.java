package com.ar.crm2.application.ai.command;

import java.util.UUID;

/**
 * Command to confirm a pending AI action proposal.
 *
 * <p><b>PR6 — resource-first tenant resolution:</b>
 * {@code empresaId} is a MANDATORY STRICT CROSS-CHECK, not the
 * authoritative tenant source. The application service loads the
 * {@code AiAccion} resource first and derives tenant authority from
 * {@code accion.empresaId}. The {@code empresaId} here is forwarded
 * from the REST boundary and MUST equal the resource's stored
 * {@code empresaId} — otherwise the service rejects with
 * {@code AsistenteTenantException.accionNoPerteneceALaEmpresaSeleccionada(...)}
 * before any persistence or authority validation runs. The actor's
 * ownership of the resource tenant is then validated via the
 * Empresa-owned {@code ActorEmpresaScopePort}.
 *
 * @param actorUsuarioId   required, the requester — must equal accion.solicitadaPor
 * @param accionId      the proposal to confirm
 * @param expectedVersion  the version the client believed it was confirming
 * @param empresaId        REQUIRED strict cross-check against the
 *                         resource's tenant. The command constructor
 *                         rejects {@code null} so the application
 *                         service ALWAYS compares this value with
 *                         {@code accion.empresaId} — when different,
 *                         the service rejects with a controlled 403
 *                         (never a generic AI / runtime error).
 */
public record ConfirmarAccionCommand(
    UUID actorUsuarioId,
    UUID accionId,
    int expectedVersion,
    UUID empresaId
) {

    public ConfirmarAccionCommand {
        if (actorUsuarioId == null) {
            throw new IllegalArgumentException("actorUsuarioId is required");
        }
        if (accionId == null) {
            throw new IllegalArgumentException("accionId is required");
        }
        if (expectedVersion <= 0) {
            throw new IllegalArgumentException("expectedVersion must be positive");
        }
        if (empresaId == null) {
            throw new IllegalArgumentException("empresaId is required");
        }
    }
}