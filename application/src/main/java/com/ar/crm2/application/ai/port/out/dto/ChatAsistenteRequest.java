package com.ar.crm2.application.ai.port.out.dto;

import com.ar.crm2.application.ai.port.out.projection.WhatsappMensajeResumen;

import java.util.List;
import java.util.UUID;

/**
 * Application-owned request DTO for the AI assistant generation port.
 *
 * <p>The type name clearly signals it is a request payload — not a value
 * object pulled from the AI side. The {@code Request} suffix keeps the
 * read/write asymmetry observable at the type level.
 *
 * <p>Spring AI / OpenAI types are not leaked into the application
 * layer — the port returns this record and the adapter maps to/from
 * its framework types.
 *
 * <p>The {@code transcript} field carries the persisted WhatsApp
 * transcript loaded via {@code WhatsappMensajeLecturaPort}. It is the
 * source of truth per {@code ai-assistant/spec.md} Requirement
 * "Analyze WhatsApp chat using persisted CRM data" — the assistant
 * reasons over persisted messages, not over an in-memory summary.
 *
 * <p>The {@code empresaId} MUST be the authoritative resource tenant
 * (derived from the addressed WhatsApp conversation's
 * {@code canalEmpresaId}, NOT a client-supplied hint) so downstream AI
 * tool calls and memory writes are scoped to the conversation's owning
 * company.
 */
public record ChatAsistenteRequest(
    UUID aiConversacionId,
    UUID actorUsuarioId,
    UUID empresaId,
    String waConversacionId,
    List<MensajeChat> historial,
    List<MensajeChat> memoria,
    List<WhatsappMensajeResumen> transcript,
    String resumenFacts,
    String resumenInferences,
    String kickoffUsuario
) {

    public ChatAsistenteRequest {
        if (aiConversacionId == null) {
            throw new IllegalArgumentException("aiConversacionId is required");
        }
        if (actorUsuarioId == null) {
            throw new IllegalArgumentException("actorUsuarioId is required");
        }
        if (empresaId == null) {
            throw new IllegalArgumentException("empresaId is required");
        }
        if (waConversacionId == null || waConversacionId.isBlank()) {
            throw new IllegalArgumentException("waConversacionId is required");
        }
        historial = historial == null ? List.of() : List.copyOf(historial);
        memoria = memoria == null ? List.of() : List.copyOf(memoria);
        transcript = transcript == null ? List.of() : List.copyOf(transcript);
    }
}
