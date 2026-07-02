package com.ar.crm2.application.ai.port.out.dto;

/**
 * Single message in an AI assistant transcript. Used as the wire model
 * for the AI generation port and for follow-up history.
 *
 * <p>Lives in {@code port.out.dto} so it is clearly the inbound DTO
 * for the outbound AI generation port — not a domain or model record.
 */
public record MensajeChat(
    String rol,    // USER | ASSISTANT | SYSTEM | TOOL
    String contenido,
    String toolCallJson
) {

    public MensajeChat {
        if (rol == null || rol.isBlank()) {
            throw new IllegalArgumentException("rol is required");
        }
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("contenido is required");
        }
    }
}
