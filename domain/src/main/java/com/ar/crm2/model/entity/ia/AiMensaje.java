package com.ar.crm2.model.entity.ia;

import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.model.enums.RolMensajeAi;
import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.AiMensajeId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Rich domain entity representing a single AI conversation turn.
 *
 * <p>{@code AiMensaje} stores user-assistant interaction history only.
 * It NEVER replaces {@code wa_mensaje} as the canonical WhatsApp
 * transcript. Tool calls are persisted opaquely as JSON in
 * {@code toolCallJson}; domain does not interpret tool payloads.
 *
 * <p>Identity: {@link AiMensajeId} (wraps UUID).
 * Equality: by id only.
 * Constructor is private; use static factory methods {@link #crear}
 * and {@link #reconstitute}.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiMensaje {

    @EqualsAndHashCode.Include
    private final AiMensajeId id;

    private final AiConversacionId aiConversacionId;
    private final RolMensajeAi rol;
    private final String contenido;
    private final String modelo;
    private final Integer promptTokens;
    private final Integer completionTokens;
    private final Long latencyMs;
    private final String toolCallJson;
    private final LocalDateTime creadoEn;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new AI message.
     *
     * @param contenido       assistant/user text content (required)
     * @param modelo          model identifier (optional; nullable)
     * @param toolCallJson    opaque JSON describing tool calls (optional)
     */
    public static AiMensaje crear(
            AiConversacionId aiConversacionId,
            RolMensajeAi rol,
            String contenido,
            String modelo,
            Integer promptTokens,
            Integer completionTokens,
            Long latencyMs,
            String toolCallJson,
            LocalDateTime ahora
    ) {
        DomainAssert.notNull(aiConversacionId, "aiConversacionId");
        DomainAssert.notNull(rol, "rol");
        DomainAssert.lengthBetween(contenido, "contenido", 1, 16384);
        if (promptTokens != null && promptTokens < 0) {
            throw new InvariantViolationException("El campo promptTokens no puede ser negativo.");
        }
        if (completionTokens != null && completionTokens < 0) {
            throw new InvariantViolationException("El campo completionTokens no puede ser negativo.");
        }
        if (latencyMs != null && latencyMs < 0) {
            throw new InvariantViolationException("El campo latencyMs no puede ser negativo.");
        }

        String safeContenido = contenido.trim();
        return new AiMensaje(
                AiMensajeId.create(),
                aiConversacionId,
                rol,
                safeContenido,
                modelo,
                promptTokens,
                completionTokens,
                latencyMs,
                toolCallJson,
                ahora
        );
    }

    /**
     * Reconstitutes an existing AiMensaje from persistence.
     */
    public static AiMensaje reconstitute(
            AiMensajeId id,
            AiConversacionId aiConversacionId,
            RolMensajeAi rol,
            String contenido,
            String modelo,
            Integer promptTokens,
            Integer completionTokens,
            Long latencyMs,
            String toolCallJson,
            LocalDateTime creadoEn
    ) {
        DomainAssert.notNull(id, "id");
        DomainAssert.notNull(aiConversacionId, "aiConversacionId");
        DomainAssert.notNull(rol, "rol");
        DomainAssert.notNull(creadoEn, "creadoEn");

        return new AiMensaje(
                id, aiConversacionId, rol, contenido, modelo,
                promptTokens, completionTokens, latencyMs, toolCallJson, creadoEn
        );
    }

    // ── Domain queries ────────────────────────────────────────────

    /** True when the message belongs to the supplied AI conversation. */
    public boolean perteneceA(AiConversacionId aiConversacionId) {
        DomainAssert.notNull(aiConversacionId, "aiConversacionId");
        return this.aiConversacionId.equals(aiConversacionId);
    }
}