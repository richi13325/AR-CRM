package com.ar.crm2.application.ai.command;

import java.util.UUID;

/**
 * Command to register a follow-up user message inside an existing AI
 * conversation and obtain the assistant's next reply.
 *
 * @param actorUsuarioId  required, the requester
 * @param aiConversacionId  the existing AI conversation id
 * @param empresaId       optional; resolved by the {@code ActorEmpresaScopePort} port when null
 * @param mensajeUsuario  the follow-up user message (required)
 */
public record RegistrarMensajeAsistenteCommand(
    UUID actorUsuarioId,
    UUID aiConversacionId,
    UUID empresaId,
    String mensajeUsuario
) {

    public RegistrarMensajeAsistenteCommand {
        if (actorUsuarioId == null) {
            throw new IllegalArgumentException("actorUsuarioId is required");
        }
        if (aiConversacionId == null) {
            throw new IllegalArgumentException("aiConversacionId is required");
        }
        if (mensajeUsuario == null || mensajeUsuario.isBlank()) {
            throw new IllegalArgumentException("mensajeUsuario is required");
        }
    }
}