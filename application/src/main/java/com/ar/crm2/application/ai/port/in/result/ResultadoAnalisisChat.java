package com.ar.crm2.application.ai.port.in.result;

import java.util.UUID;

/**
 * Result of a chat analysis or follow-up turn.
 *
 * <p>Carries the AI conversation id and the assistant reply text
 * only. Tool-driven proposal staging happens through the
 * {@code ProponerAccionTool} -> {@code ProponerAccionUseCase} path;
 * this result deliberately does NOT include a list of staged
 * actions, so the analyze / follow-up paths stay as thin
 * coordinators that own conversation / summary persistence.
 *
 * @param aiConversacionId  the (possibly newly created) AI conversation id
 */
public record ResultadoAnalisisChat(
    UUID aiConversacionId,
    String contenidoAsistente,
    String modelo
) {

    public ResultadoAnalisisChat {
        if (aiConversacionId == null) {
            throw new IllegalArgumentException("aiConversacionId is required");
        }
        contenidoAsistente = contenidoAsistente == null ? "" : contenidoAsistente;
    }
}
