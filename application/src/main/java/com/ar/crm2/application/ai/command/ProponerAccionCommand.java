package com.ar.crm2.application.ai.command;

import com.ar.crm2.model.enums.TipoAccion;

/**
 * Command sent from an AI tool to the {@code ProponerAccionUseCase}.
 *
 * <p>The tool adapter (Spring AI {@code @Tool} input adapter) builds
 * this command from the model-supplied proposal shape. The use case
 * then enriches it with the trusted scope (actor / tenant / conv ids)
 * resolved through the
 * {@code AiToolContextPort} outbound port — those fields MUST
 * NOT come from the model payload.
 *
 * <p>Unlike {@link RegistrarAccionCommand}, this command carries only
 * the model-supplied proposal shape (tipo, payload, rationale, ttl).
 * Scope fields are intentionally absent: they are owned by the
 * resolver and stitched in by the application service to enforce the
 * safety boundary.
 *
 * @param tipo        typed discriminator — the matching CRM command /
 *                    payload schema will be selected at confirmation time
 * @param payloadJson JSON object payload matching the schema for the
 *                    selected tipo
 * @param rationale   short, plain-language rationale explaining why the
 *                    assistant is proposing this action
 * @param ttlMinutos  minutes until the staged proposal expires; must
 *                    be positive
 */
public record ProponerAccionCommand(
    TipoAccion tipo,
    String payloadJson,
    String rationale,
    int ttlMinutos
) {

    public ProponerAccionCommand {
        if (tipo == null) {
            throw new IllegalArgumentException("tipo is required");
        }
        if (payloadJson == null || payloadJson.isBlank()) {
            throw new IllegalArgumentException("payloadJson is required");
        }
        if (rationale == null || rationale.isBlank()) {
            throw new IllegalArgumentException("rationale is required");
        }
        if (ttlMinutos <= 0) {
            throw new IllegalArgumentException("ttlMinutos must be positive");
        }
    }
}
