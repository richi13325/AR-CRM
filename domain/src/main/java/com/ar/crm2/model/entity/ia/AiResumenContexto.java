package com.ar.crm2.model.entity.ia;

import com.ar.crm2.model.vo.AiConversacionId;
import com.ar.crm2.model.vo.AiResumenContextoId;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;
import com.ar.crm2.model.vo.UsuarioId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Rich domain entity representing the persistent context summary of an
 * AI assistant session.
 *
 * <p>Stores compressed business facts and model inferences so that
 * follow-up turns do not need to resend the full transcript. The
 * WhatsApp source ({@code wa_mensaje}) is always authoritative — if a
 * summary claim conflicts with a source message, the source wins.
 *
 * <p>Identity: {@link AiResumenContextoId} (wraps UUID).
 * Equality: by id only.
 * Constructor is private; use static factory methods {@link #crear}
 * and {@link #reconstitute}.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiResumenContexto {

    @EqualsAndHashCode.Include
    private final AiResumenContextoId id;

    private final UsuarioId actorUsuarioId;
    private final EmpresaId empresaId;
    private final String waConversacionId;
    private final ContactoId contactoId;
    private final String facts;
    private final String inferences;
    private final String sourceWaMensajeId;
    private final long sourceWatermark;
    private final AiConversacionId aiConversacionId;
    private final LocalDateTime creadoEn;
    private final LocalDateTime actualizadoEn;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new context summary for the supplied scope.
     *
     * @param facts          observed facts (plain text)
     * @param inferences     model inferences (plain text)
     * @param sourceWatermark monotonic counter for the latest source
     *                       message observed when this summary was
     *                       generated; higher values are fresher
     */
    public static AiResumenContexto crear(
            UsuarioId actorUsuarioId,
            EmpresaId empresaId,
            String waConversacionId,
            ContactoId contactoId,
            String facts,
            String inferences,
            String sourceWaMensajeId,
            long sourceWatermark,
            AiConversacionId aiConversacionId,
            LocalDateTime ahora
    ) {
        DomainAssert.notNull(actorUsuarioId, "actorUsuarioId");
        DomainAssert.notNull(empresaId, "empresaId");
        DomainAssert.notBlank(waConversacionId, "waConversacionId");
        DomainAssert.notNull(aiConversacionId, "aiConversacionId");
        DomainAssert.lengthBetween(facts, "facts", 1, 16384);
        DomainAssert.lengthBetween(inferences, "inferences", 1, 16384);
        if (sourceWatermark < 0) {
            throw new com.ar.crm2.exception.InvariantViolationException(
                    "El campo sourceWatermark no puede ser negativo."
            );
        }

        return new AiResumenContexto(
                AiResumenContextoId.create(),
                actorUsuarioId,
                empresaId,
                waConversacionId.trim(),
                contactoId,
                facts.trim(),
                inferences.trim(),
                sourceWaMensajeId,
                sourceWatermark,
                aiConversacionId,
                ahora,
                ahora
        );
    }

    /**
     * Reconstitutes an existing AiResumenContexto from persistence.
     */
    public static AiResumenContexto reconstitute(
            AiResumenContextoId id,
            UsuarioId actorUsuarioId,
            EmpresaId empresaId,
            String waConversacionId,
            ContactoId contactoId,
            String facts,
            String inferences,
            String sourceWaMensajeId,
            long sourceWatermark,
            AiConversacionId aiConversacionId,
            LocalDateTime creadoEn,
            LocalDateTime actualizadoEn
    ) {
        DomainAssert.notNull(id, "id");
        DomainAssert.notNull(actorUsuarioId, "actorUsuarioId");
        DomainAssert.notNull(empresaId, "empresaId");
        DomainAssert.notBlank(waConversacionId, "waConversacionId");
        DomainAssert.notNull(aiConversacionId, "aiConversacionId");
        DomainAssert.notNull(creadoEn, "creadoEn");

        return new AiResumenContexto(
                id, actorUsuarioId, empresaId, waConversacionId, contactoId,
                facts, inferences, sourceWaMensajeId, sourceWatermark,
                aiConversacionId, creadoEn, actualizadoEn
        );
    }

    /**
     * Returns a new summary with updated facts/inferences and bumped
     * watermark. Use when a follow-up turn supersedes an existing
     * summary.
     */
    public AiResumenContexto reemplazarCon(
            String facts,
            String inferences,
            String sourceWaMensajeId,
            long sourceWatermark,
            LocalDateTime ahora
    ) {
        DomainAssert.notNull(ahora, "ahora");
        DomainAssert.lengthBetween(facts, "facts", 1, 16384);
        DomainAssert.lengthBetween(inferences, "inferences", 1, 16384);
        if (sourceWatermark < this.sourceWatermark) {
            throw new com.ar.crm2.exception.InvariantViolationException(
                    "El nuevo sourceWatermark no puede ser menor al actual."
            );
        }

        return new AiResumenContexto(
                this.id, actorUsuarioId, empresaId, waConversacionId, contactoId,
                facts.trim(), inferences.trim(), sourceWaMensajeId, sourceWatermark,
                aiConversacionId, creadoEn, ahora
        );
    }

    // ── Domain queries ────────────────────────────────────────────

    public boolean perteneceA(UsuarioId usuarioId) {
        DomainAssert.notNull(usuarioId, "usuarioId");
        return this.actorUsuarioId.equals(usuarioId);
    }

    public boolean perteneceA(EmpresaId empresaId) {
        DomainAssert.notNull(empresaId, "empresaId");
        return this.empresaId.equals(empresaId);
    }

    /**
     * True when the supplied watermark is fresher (strictly greater) than
     * the watermark recorded at summary time — used to decide if the
     * summary must be regenerated before the next turn.
     */
    public boolean esStale(long currentWatermark) {
        return currentWatermark > this.sourceWatermark;
    }
}