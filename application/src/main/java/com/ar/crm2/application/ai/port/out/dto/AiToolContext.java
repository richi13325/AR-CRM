package com.ar.crm2.application.ai.port.out.dto;

import java.util.UUID;

/**
 * Application-owned context returned by the
 * {@code AiToolContextPort} outbound port. Carries the trusted
 * actor / tenant / conv ids that an AI tool needs to build a staging
 * command.
 *
 * <p>Tools are model-driven; the model supplies only the proposal
 * payload (tipo, payloadJson, rationale). The actor/tenant scope is
 * resolved upstream from MDC/ThreadLocal by an infrastructure resolver
 * (PR 4) that flows the request-scoped {@code ActorContext} and AI
 * conversation id into the tool invocation. This record is the
 * contract that the resolver exposes to the application.
 *
 * <p>Lives in {@code application.ai.port.out.dto} because it is the
 * return value of an outbound port; it is NOT a domain entity and
 * contains no Spring, HTTP, or framework types.
 *
 * @param actorUsuarioId   requester user id (matches the proposal's
 *                         {@code solicitadaPor} and the same value
 *                         {@code ConfirmarAccionUseCase} verifies on
 *                         owner-only confirmation)
 * @param empresaId        tenant company id the chat/contact belongs to
 * @param aiConversacionId id of the {@code AiConversacion} the tool is
 *                         running inside (audit linkage)
 * @param waConversacionId source WhatsApp conversation id (audit linkage
 *                         — matches the value persisted on the proposal)
 */
public record AiToolContext(
    UUID actorUsuarioId,
    UUID empresaId,
    UUID aiConversacionId,
    String waConversacionId
) {

    public AiToolContext {
        if (actorUsuarioId == null) {
            throw new IllegalArgumentException("actorUsuarioId is required");
        }
        if (empresaId == null) {
            throw new IllegalArgumentException("empresaId is required");
        }
        if (aiConversacionId == null) {
            throw new IllegalArgumentException("aiConversacionId is required");
        }
        if (waConversacionId == null || waConversacionId.isBlank()) {
            throw new IllegalArgumentException("waConversacionId is required");
        }
    }
}
