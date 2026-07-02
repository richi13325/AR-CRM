package com.ar.crm2.adapter.in.tool.ai.dto;

import org.springframework.ai.tool.annotation.ToolParam;

/**
 * Infrastructure DTO passed by the model to the
 * {@code obtenerMensajesRecientes} AI tool.
 *
 * <p><b>Trust boundary.</b> the request DTO carries ONLY the
 * {@code limit} cap. The {@code waConversacionId} is intentionally
 * absent from the wire contract — it is resolved at tool-invocation
 * time from the trusted {@code AiToolContextPort}, never from the
 * model payload. This prevents the model from probing foreign
 * WhatsApp conversations.
 *
 * <p>{@code limit} is intentionally bounded so the model cannot ask
 * for an unbounded scan of the transcript (which would balloon the
 * prompt size and the per-call cost).
 */
public record ObtenerMensajesRecientesRequest(
    @ToolParam(description = "Maximum number of recent messages to return. Must be a "
        + "positive integer between 1 and 50. The tool returns the most recent N "
        + "messages from the transcript of the conversation the assistant is bound to, "
        + "ordered chronologically ascending.",
        required = true)
    int limit
) {

    public ObtenerMensajesRecientesRequest {
        if (limit < 1 || limit > 50) {
            throw new IllegalArgumentException("limit must be between 1 and 50");
        }
    }
}