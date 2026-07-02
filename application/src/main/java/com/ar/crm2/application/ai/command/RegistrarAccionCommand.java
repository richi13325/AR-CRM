package com.ar.crm2.application.ai.command;

import com.ar.crm2.model.enums.TipoAccion;

import java.util.UUID;

/**
 * Command to register a new AI action proposal in PENDING state.
 *
 * <p>Two construction entry points:
 * <ul>
 *   <li>{@link #RegistrarAccionCommand} — String discriminator
 *       (used by adapters that already have a JSON string).</li>
 *   <li>{@link #conTipoAccion} — typed {@link TipoAccion}
 *       (used by application services that already validated the
 *       payload against the matching typed payload record).</li>
 * </ul>
 *
 * <p>Represents the staging of a sensitive CRM mutation suggested by
 * the assistant (or a tool). The use case does NOT invoke real CRM
 * mutation use cases; that is the exclusive job of
 * {@code ConfirmarAccionUseCase}.
 *
 * <p><b>Audit linkage:</b> every proposal carries the source
 * {@code aiConversacionId} (required) and an optional
 * {@code waMensajeId} when the draft was triggered by a specific
 * WhatsApp message. The persistence layer persists both so the audit
 * trail can reconstruct exactly which AI turn produced the proposal.
 */
public record RegistrarAccionCommand(
    UUID actorUsuarioId,
    UUID empresaId,
    UUID aiConversacionId,
    String waConversacionId,
    String waMensajeId,
    String tipoAccion,
    String payloadJson,
    String rationale,
    int ttlMinutos
) {

    public RegistrarAccionCommand {
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
        // Normalize empty string to null so the DB layer does not see "" for "no source message".
        if (waMensajeId != null && waMensajeId.isBlank()) {
            waMensajeId = null;
        }
    }

    /**
     * Builds the command from a typed {@link TipoAccion}.
     * The discriminator is stored as the enum's {@code name}.
     */
    public static RegistrarAccionCommand conTipoAccion(
            UUID actorUsuarioId,
            UUID empresaId,
            UUID aiConversacionId,
            String waConversacionId,
            TipoAccion tipo,
            String payloadJson,
            String rationale,
            int ttlMinutos
    ) {
        return new RegistrarAccionCommand(
                actorUsuarioId, empresaId, aiConversacionId, waConversacionId,
                null, tipo.name(), payloadJson, rationale, ttlMinutos
        );
    }
}