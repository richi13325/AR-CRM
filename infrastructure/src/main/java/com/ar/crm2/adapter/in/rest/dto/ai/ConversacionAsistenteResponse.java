package com.ar.crm2.adapter.in.rest.dto.ai;

import com.ar.crm2.application.ai.port.in.ObtenerConversacionAsistenteUseCase.ResultadoConversacionAsistente;

import java.util.List;
import java.util.UUID;

/**
 * Response shape for {@code GET /api/ai/conversaciones/{id}}.
 *
 * <p>Carries the AI conversation aggregate (id + actor + scope) and
 * its ordered message history. The {@code mensajes} list is already
 * ordered chronologically by the application service.
 */
public record ConversacionAsistenteResponse(
    ConversacionSummary conversacion,
    List<MensajeSummary> mensajes
) {

    /**
     * Surface-level projection of {@link com.ar.crm2.model.entity.ia.AiConversacion}.
     */
    public record ConversacionSummary(
        UUID id,
        UUID empresaId,
        UUID actorUsuarioId,
        String waConversacionId,
        boolean archivada,
        String creadoEn,
        String actualizadoEn
    ) {}

    /**
     * Surface-level projection of {@link com.ar.crm2.model.entity.ia.AiMensaje}.
     * The assistant / user / tool / system role is exposed as a string.
     */
    public record MensajeSummary(
        UUID id,
        String rol,
        String contenido,
        String modelo,
        Integer promptTokens,
        Integer completionTokens,
        Long latencyMs,
        String creadoEn
    ) {}

    public static ConversacionAsistenteResponse fromDomain(ResultadoConversacionAsistente result) {
        var conv = result.conversacion();
        ConversacionSummary convSummary = new ConversacionSummary(
                conv.getId().value(),
                conv.getEmpresaId().value(),
                conv.getActorUsuarioId().value(),
                conv.getWaConversacionId(),
                conv.isArchivada(),
                conv.getCreadoEn().toString(),
                conv.getActualizadoEn().toString()
        );
        List<MensajeSummary> msgs = result.mensajes().stream()
                .map(m -> new MensajeSummary(
                        m.getId().value(),
                        m.getRol().name(),
                        m.getContenido(),
                        m.getModelo(),
                        m.getPromptTokens(),
                        m.getCompletionTokens(),
                        m.getLatencyMs(),
                        m.getCreadoEn().toString()
                ))
                .toList();
        return new ConversacionAsistenteResponse(convSummary, msgs);
    }
}