package com.ar.crm2.application.ai.port.out.dto;

/**
 * Application-owned response DTO from the AI generation port.
 *
 * <p>The assistant text is just a friendly reply. Tool-driven
 * proposal staging is NOT the job of this DTO — the AI invokes the
 * tool directly through Spring AI's tool-calling mechanism, and
 * the tool delegates to {@code ProponerAccionUseCase}. This DTO
 * carries only the assistant reply text + framework metadata
 * (model, tokens, latency). The application never reads actions
 * from this response: the only place that stages proposals is the
 * tool path.
 */
public record RespuestaAsistente(
    String contenido,
    String modelo,
    Integer promptTokens,
    Integer completionTokens,
    Long latencyMs
) {

    public RespuestaAsistente {
        contenido = contenido == null ? "" : contenido;
    }
}
