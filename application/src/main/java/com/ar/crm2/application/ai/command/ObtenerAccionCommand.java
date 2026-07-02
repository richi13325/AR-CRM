package com.ar.crm2.application.ai.command;

import java.util.UUID;

/**
 * Command to fetch a single AI action proposal by id.
 *
 * <p>Tenant scope is enforced inside the use case via the Empresa-
 * owned {@code ActorEmpresaScopePort} — the actor must own the
 * proposal's {@code empresaId}.
 *
 * @param actorUsuarioId  required
 * @param accionId     required
 * @param empresaId       optional; resolved by the {@code ActorEmpresaScopePort} port when null
 */
public record ObtenerAccionCommand(
    UUID actorUsuarioId,
    UUID accionId,
    UUID empresaId
) {

    public ObtenerAccionCommand {
        if (actorUsuarioId == null) {
            throw new IllegalArgumentException("actorUsuarioId is required");
        }
        if (accionId == null) {
            throw new IllegalArgumentException("accionId is required");
        }
    }
}
