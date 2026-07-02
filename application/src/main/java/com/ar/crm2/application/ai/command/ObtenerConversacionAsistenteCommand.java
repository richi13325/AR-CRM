package com.ar.crm2.application.ai.command;

import java.util.UUID;

/**
 * Command to fetch a single AI conversation by id, scoped to the
 * requester.
 *
 * <p><b>PR6 — resource-first tenant resolution:</b>
 * {@code empresaId} is a MANDATORY STRICT CROSS-CHECK, not the
 * authoritative tenant source. The application service loads the
 * {@code AiConversacion} resource first and derives tenant authority
 * from {@code conversacion.empresaId}. The {@code empresaId} here is
 * forwarded from the REST boundary and MUST equal the resource's
 * stored {@code empresaId} — otherwise the service rejects with
 * {@code AsistenteTenantException.conversacionNoPerteneceALaEmpresaSeleccionada(...)}
 * before any persistence or authority validation runs. The actor's
 * ownership of the resource tenant is then validated via the
 * Empresa-owned {@code ActorEmpresaScopePort}.
 *
 * @param actorUsuarioId  required
 * @param aiConversacionId  required
 * @param empresaId       REQUIRED strict cross-check against the
 *                        resource's tenant. The command constructor
 *                        rejects {@code null} so the application
 *                        service ALWAYS compares this value with
 *                        {@code conversacion.empresaId} — when different,
 *                        the service rejects with a controlled 403
 *                        (never a generic AI / runtime error).
 */
public record ObtenerConversacionAsistenteCommand(
    UUID actorUsuarioId,
    UUID aiConversacionId,
    UUID empresaId
) {

    public ObtenerConversacionAsistenteCommand {
        if (actorUsuarioId == null) {
            throw new IllegalArgumentException("actorUsuarioId is required");
        }
        if (aiConversacionId == null) {
            throw new IllegalArgumentException("aiConversacionId is required");
        }
        if (empresaId == null) {
            throw new IllegalArgumentException("empresaId is required");
        }
    }
}
