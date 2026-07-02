package com.ar.crm2.adapter.in.rest.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound REST request for {@code POST /api/ai/chat}.
 *
 * <p>Wraps the {@link com.ar.crm2.application.ai.command.AnalizarChatCommand}
 * payload from the request body. The actor identity is sourced from
 * the {@link com.ar.crm2.application.security.ActorContext} request
 * attribute populated by
 * {@link com.ar.crm2.security.ActorContextRequestAttributeFilter}, NEVER
 * from the body — that would let a model payload override the
 * trusted actor.
 *
 * @param waConversacionId  the source WhatsApp conversation id (required)
 * @param mensajeUsuario    optional kick-off question; nullable / blank
 *                          means the request is a "first turn" without
 *                          user prompt
 */
public record AnalizarChatRequest(
    @NotBlank @Size(max = 200) String waConversacionId,
    @Size(max = 16384) String mensajeUsuario
) {
}