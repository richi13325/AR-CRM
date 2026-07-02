package com.ar.crm2.adapter.in.tool.ai.dto;

import java.time.LocalDateTime;

/**
 * Infrastructure DTO returned by the {@code obtenerResumenChat}
 * AI tool.
 *
 * <p>All fields are nullable: when no summary exists yet for the
 * AI conversation the tool returns a placeholder record (so the
 * model can still reason about the conversation context). The
 * version / messages-covered fields reflect the persisted
 * {@code AiResumenContexto} when one exists.
 */
public record ObtenerResumenChatResponse(
    String facts,
    String inferences,
    Long sourceWatermark,
    LocalDateTime actualizadoEn
) {
}