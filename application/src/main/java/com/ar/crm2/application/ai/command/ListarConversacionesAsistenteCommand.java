package com.ar.crm2.application.ai.command;

import java.util.UUID;

/**
 * Command to list AI conversations for the requester scope.
 *
 * <p>Project convention uses {@code *Command} even for reads; there
 * is no separate {@code query} package. The naming follows
 * {@code GetContactoByIdCommand} and similar read commands in the
 * CRM modules.
 *
 * @param actorUsuarioId required
 * @param empresaId     optional; resolved by the {@code ActorEmpresaScopePort} port when null
 * @param limite        max conversations to return (1..200)
 */
public record ListarConversacionesAsistenteCommand(
    UUID actorUsuarioId,
    UUID empresaId,
    int limite
) {

    public ListarConversacionesAsistenteCommand {
        if (actorUsuarioId == null) {
            throw new IllegalArgumentException("actorUsuarioId is required");
        }
        if (limite <= 0 || limite > 200) {
            throw new IllegalArgumentException("limite must be between 1 and 200");
        }
    }
}
