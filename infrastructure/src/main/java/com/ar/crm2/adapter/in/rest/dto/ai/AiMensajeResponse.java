package com.ar.crm2.adapter.in.rest.dto.ai;

import com.ar.crm2.application.ai.port.in.result.ResultadoAnalisisChat;

import java.util.UUID;

/**
 * Response shape for {@code POST /api/ai/chat}.
 *
 * <p>Mirrors {@link ResultadoAnalisisChat} but only the JSON-friendly
 * fields the client needs — the conversation id, the assistant reply
 * text, and the model id used. No Spring, application or domain types
 * leak into the wire shape.
 */
public record AiMensajeResponse(
    UUID aiConversacionId,
    String contenidoAsistente,
    String modelo
) {

    public static AiMensajeResponse fromDomain(ResultadoAnalisisChat result) {
        return new AiMensajeResponse(
                result.aiConversacionId(),
                result.contenidoAsistente(),
                result.modelo()
        );
    }
}