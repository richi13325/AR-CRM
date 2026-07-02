package com.ar.crm2.application.ai.command;

import java.util.UUID;

/**
 * Command to analyze a WhatsApp conversation in an AI-assisted way.
 *
 * <p><b>Resource-first tenant resolution (PR5):</b> the
 * {@code empresaId} on this command is an OPTIONAL hint only. Tenant
 * authority for {@code /chat} is ALWAYS derived from the addressed
 * WhatsApp conversation resource ({@code canalEmpresaId}) — the
 * application service never uses the hint as the authoritative
 * tenant source. If the resource tenant is not owned by the
 * authenticated actor, the request is rejected before any
 * {@code ai_*} row is created.
 *
 * <p>The hint MAY be used as a cross-check or for logging, but MUST
 * NOT override the resource tenant.
 *
 * @param actorUsuarioId   required, the requester (identity anchor)
 * @param empresaId        optional hint / cross-check only — MUST NOT
 *                         override the resource tenant authority
 *                         derived from {@code canalEmpresaId}
 * @param waConversacionId required, the source WhatsApp conversation id
 * @param mensajeUsuario   optional free-form kick-off question (nullable)
 */
public record AnalizarChatCommand(
    UUID actorUsuarioId,
    UUID empresaId,
    String waConversacionId,
    String mensajeUsuario
) {

    public AnalizarChatCommand {
        if (actorUsuarioId == null) {
            throw new IllegalArgumentException("actorUsuarioId is required");
        }
        if (waConversacionId == null || waConversacionId.isBlank()) {
            throw new IllegalArgumentException("waConversacionId is required");
        }
    }
}