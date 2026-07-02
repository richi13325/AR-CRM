package com.ar.crm2.adapter.in.tool.ai.dto;

import org.springframework.ai.tool.annotation.ToolParam;

/**
 * Infrastructure DTO passed by the model to the {@code proponerAccion}
 * AI tool.
 *
 * <p>Lives outside the tool class (in its own package) because it is
 * an infrastructure wire-shape — it carries {@link ToolParam} metadata
 * so Spring AI publishes a JSON schema to the model. The model uses
 * the descriptions to choose well-formed values. This shape is purely
 * an infrastructure concern and never crosses the application boundary;
 * the {@code ProponerAccionToolMapper} translates it into an
 * application-owned {@code ProponerAccionCommand}.
 *
 * <p>Constructor validation rejects blank/null values up-front so
 * Spring AI surfaces a clear error to the model instead of an opaque
 * {@code NullPointerException} later in the staging use case. The
 * TTL must be positive; negative or zero values are rejected with a
 * clear message.
 */
public record ProponerAccionRequest(
    @ToolParam(description = "Action discriminator. One of: "
        + "CREATE_CONTACTO, CREATE_TRATO, CREATE_TAREA, MOVE_KANBAN_FICHA.",
        required = true)
    String tipoAccion,

    @ToolParam(description = "JSON object payload matching the schema for the "
        + "selected tipoAccion. Example for CREATE_TAREA: "
        + "{\"titulo\":\"...\",\"descripcion\":\"...\",\"dueAt\":\"...\"}.",
        required = true)
    String payloadJson,

    @ToolParam(description = "Short, plain-language rationale explaining why the "
        + "assistant is proposing this action. The user sees this in the "
        + "confirmation dialog.",
        required = true)
    String rationale,

    @ToolParam(description = "Minutes until the staged proposal expires. "
        + "Must be a positive integer; the proposal is marked EXPIRED "
        + "automatically after the TTL elapses if the user has not "
        + "confirmed it yet.",
        required = true)
    int ttlMinutos
) {

    public ProponerAccionRequest {
        if (tipoAccion == null || tipoAccion.isBlank()) {
            throw new IllegalArgumentException("tipoAccion is required");
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
