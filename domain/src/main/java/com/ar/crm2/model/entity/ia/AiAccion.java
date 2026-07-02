package com.ar.crm2.model.entity.ia;

import com.ar.crm2.exception.AccionStateTransitionException;
import com.ar.crm2.model.enums.EstadoAccion;
import com.ar.crm2.model.vo.AiAccionId;
import com.ar.crm2.model.vo.AiConversacionId;
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
 * Rich domain entity representing an AI-suggested CRM action proposal.
 *
 * <p>This is the safety boundary for AI-driven CRM mutations. Models and
 * tools may stage proposals in PENDING state, but only the original
 * requester (validated via {@code solicitadaPor} in application layer)
 * may transition to CONFIRMED — at which point the confirmation use case
 * is the only path that invokes real mutation use cases.
 *
 * <p>Identity: {@link AiAccionId} (wraps UUID).
 * Equality: by id only.
 * No public setters — state changes go through business methods that
 * preserve invariants and bump the optimistic-lock {@code version}.
 * Constructor is private; use static factory methods {@link #crear}
 * and {@link #reconstitute}.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiAccion {

    @EqualsAndHashCode.Include
    private final AiAccionId id;

    private final EmpresaId empresaId;
    private final UsuarioId solicitadaPor;
    private final String waConversacionId;
    private final String waMensajeId;
    private final AiConversacionId aiConversacionId;
    private final String tipoAccion;
    private final String payloadJson;
    private final String rationale;
    private final int version;
    private final LocalDateTime expiresAt;
    private final String resultadoEntidadId;
    private final String errorReason;
    private final EstadoAccion estado;
    private final LocalDateTime creadoEn;
    private final LocalDateTime actualizadoEn;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Stages a new AI action proposal in PENDING state.
     *
     * <p>Generates id, version=1, timestamps. Required fields are
     * validated. The {@code payloadJson} is preserved opaquely — the
     * domain does not interpret its content (validation lives at the
     * application boundary per TipoAccion schema).
     *
     * @param ahora             the current time (injected to keep tests deterministic)
     * @param ttl               minutes until the proposal expires; must be positive
     * @throws com.ar.crm2.exception.InvariantViolationException on null/blank required fields
     */
    public static AiAccion crear(
            EmpresaId empresaId,
            UsuarioId solicitadaPor,
            String waConversacionId,
            String waMensajeId,
            AiConversacionId aiConversacionId,
            String tipoAccion,
            String payloadJson,
            String rationale,
            int ttl,
            LocalDateTime ahora
    ) {
        DomainAssert.notNull(empresaId, "empresaId");
        DomainAssert.notNull(solicitadaPor, "solicitadaPor");
        DomainAssert.notBlank(waConversacionId, "waConversacionId");
        DomainAssert.notNull(aiConversacionId, "aiConversacionId");
        DomainAssert.lengthBetween(tipoAccion, "tipoAccion", 1, 50);
        DomainAssert.lengthBetween(payloadJson, "payloadJson", 1, 16384);
        DomainAssert.lengthBetween(rationale, "rationale", 1, 2000);
        if (waMensajeId != null && waMensajeId.isBlank()) {
            throw new com.ar.crm2.exception.InvariantViolationException(
                    "El campo waMensajeId no puede ser blank; use null cuando no aplique."
            );
        }
        if (ttl <= 0) {
            throw new com.ar.crm2.exception.InvariantViolationException(
                    "El campo ttl debe ser mayor que cero."
            );
        }

        return new AiAccion(
                AiAccionId.create(),
                empresaId,
                solicitadaPor,
                waConversacionId.trim(),
                waMensajeId == null ? null : waMensajeId.trim(),
                aiConversacionId,
                tipoAccion.trim(),
                payloadJson,
                rationale.trim(),
                1,
                ahora.plusMinutes(ttl),
                null,
                null,
                EstadoAccion.PENDING,
                ahora,
                ahora
        );
    }

    /**
     * Reconstitutes an existing AiAccion from persistence.
     * No state-machine validation — the caller is the persistence layer
     * which preserves the same invariants on write.
     */
    public static AiAccion reconstitute(
            AiAccionId id,
            EmpresaId empresaId,
            UsuarioId solicitadaPor,
            String waConversacionId,
            String waMensajeId,
            AiConversacionId aiConversacionId,
            String tipoAccion,
            String payloadJson,
            String rationale,
            int version,
            LocalDateTime expiresAt,
            String resultadoEntidadId,
            String errorReason,
            EstadoAccion estado,
            LocalDateTime creadoEn,
            LocalDateTime actualizadoEn
    ) {
        DomainAssert.notNull(id, "id");
        DomainAssert.notNull(empresaId, "empresaId");
        DomainAssert.notNull(solicitadaPor, "solicitadaPor");
        DomainAssert.notNull(creadoEn, "creadoEn");
        DomainAssert.notNull(estado, "estado");
        DomainAssert.notBlank(waConversacionId, "waConversacionId");

        return new AiAccion(
                id,
                empresaId,
                solicitadaPor,
                waConversacionId,
                waMensajeId,
                aiConversacionId,
                tipoAccion,
                payloadJson,
                rationale,
                version,
                expiresAt,
                resultadoEntidadId,
                errorReason,
                estado,
                creadoEn,
                actualizadoEn
        );
    }

    // ── State machine ─────────────────────────────────────────────

    /**
     * Confirms a PENDING proposal. Only the original requester may invoke
     * this; ownership is validated in the application service before
     * dispatching here. Returns a new instance in CONFIRMED state with
     * version incremented.
     *
     * @throws AccionStateTransitionException if estado != PENDING
     */
    public AiAccion confirmar(LocalDateTime ahora) {
        DomainAssert.notNull(ahora, "ahora");
        if (this.estado != EstadoAccion.PENDING) {
            throw AccionStateTransitionException.transicionNoPermitida(estado.name(), "confirmar");
        }
        return new AiAccion(
                id, empresaId, solicitadaPor, waConversacionId, waMensajeId, aiConversacionId,
                tipoAccion, payloadJson, rationale, version + 1, expiresAt,
                resultadoEntidadId, errorReason,
                EstadoAccion.CONFIRMED, creadoEn, ahora
        );
    }

    /**
     * Rejects a PENDING proposal. Side-effect free at the domain level;
     * no CRM entity is created, updated or moved.
     *
     * @throws AccionStateTransitionException if estado != PENDING
     */
    public AiAccion rechazar(LocalDateTime ahora) {
        DomainAssert.notNull(ahora, "ahora");
        if (this.estado != EstadoAccion.PENDING) {
            throw AccionStateTransitionException.transicionNoPermitida(estado.name(), "rechazar");
        }
        return new AiAccion(
                id, empresaId, solicitadaPor, waConversacionId, waMensajeId, aiConversacionId,
                tipoAccion, payloadJson, rationale, version + 1, expiresAt,
                resultadoEntidadId, errorReason,
                EstadoAccion.REJECTED, creadoEn, ahora
        );
    }

    /**
     * Marks a PENDING proposal as expired. Idempotent when called on an
     * already EXPIRED proposal (returns same instance).
     *
     * @throws AccionStateTransitionException if estado is in a non-PENDING, non-EXPIRED state
     */
    public AiAccion expirar(LocalDateTime ahora) {
        DomainAssert.notNull(ahora, "ahora");
        if (this.estado == EstadoAccion.EXPIRED) {
            return this;
        }
        if (this.estado != EstadoAccion.PENDING) {
            throw AccionStateTransitionException.transicionNoPermitida(estado.name(), "expirar");
        }
        return new AiAccion(
                id, empresaId, solicitadaPor, waConversacionId, waMensajeId, aiConversacionId,
                tipoAccion, payloadJson, rationale, version + 1, expiresAt,
                resultadoEntidadId, errorReason,
                EstadoAccion.EXPIRED, creadoEn, ahora
        );
    }

    /**
     * Marks a CONFIRMED proposal as EXECUTED once the real CRM mutation
     * succeeds. Records the resulting entity id for audit.
     *
     * @throws AccionStateTransitionException if estado != CONFIRMED
     */
    public AiAccion marcarEjecutada(String resultadoEntidadId, LocalDateTime ahora) {
        DomainAssert.notBlank(resultadoEntidadId, "resultadoEntidadId");
        DomainAssert.notNull(ahora, "ahora");
        if (this.estado != EstadoAccion.CONFIRMED) {
            throw AccionStateTransitionException.transicionNoPermitida(estado.name(), "marcar ejecutada");
        }
        return new AiAccion(
                id, empresaId, solicitadaPor, waConversacionId, waMensajeId, aiConversacionId,
                tipoAccion, payloadJson, rationale, version + 1, expiresAt,
                resultadoEntidadId.trim(), errorReason,
                EstadoAccion.EXECUTED, creadoEn, ahora
        );
    }

    /**
     * Marks a CONFIRMED proposal as FAILED when the real CRM mutation
     * fails. Records the error reason for audit. The failure is recorded
     * before returning so the proposal lifecycle is always observable.
     *
     * @throws AccionStateTransitionException if estado != CONFIRMED
     */
    public AiAccion marcarFallida(String errorReason, LocalDateTime ahora) {
        DomainAssert.lengthBetween(errorReason, "errorReason", 1, 1000);
        DomainAssert.notNull(ahora, "ahora");
        if (this.estado != EstadoAccion.CONFIRMED) {
            throw AccionStateTransitionException.transicionNoPermitida(estado.name(), "marcar fallida");
        }
        return new AiAccion(
                id, empresaId, solicitadaPor, waConversacionId, waMensajeId, aiConversacionId,
                tipoAccion, payloadJson, rationale, version + 1, expiresAt,
                resultadoEntidadId, errorReason.trim(),
                EstadoAccion.FAILED, creadoEn, ahora
        );
    }

    // ── Domain queries ─────────────────────────────────────────────

    /**
     * Returns true when the proposal has passed its expiry time.
     * PENDING-only check: terminal states have their own semantics.
     */
    public boolean estaExpirada(LocalDateTime ahora) {
        DomainAssert.notNull(ahora, "ahora");
        return this.estado == EstadoAccion.PENDING && !ahora.isBefore(this.expiresAt);
    }

    /**
     * Returns true when the entity id of the proposal's requester
     * matches the supplied id. Used by the application layer to enforce
     * that only the original requester may confirm.
     */
    public boolean perteneceA(UsuarioId actorUsuarioId) {
        DomainAssert.notNull(actorUsuarioId, "actorUsuarioId");
        return this.solicitadaPor.equals(actorUsuarioId);
    }

    /**
     * Returns true when the proposal belongs to a company the actor
     * owns (creadoPor). Application layer cross-checks this against
     * {@code FindEmpresasByCreadorPort}.
     */
    public boolean perteneceA(EmpresaId empresaId) {
        DomainAssert.notNull(empresaId, "empresaId");
        return this.empresaId.equals(empresaId);
    }

    /**
     * Returns true when the proposal is in a terminal state and cannot
     * transition further.
     */
    public boolean esTerminal() {
        return this.estado == EstadoAccion.REJECTED
                || this.estado == EstadoAccion.EXPIRED
                || this.estado == EstadoAccion.EXECUTED
                || this.estado == EstadoAccion.FAILED;
    }

    // ── Policy checks ───────────────────────────────────────────
    //
    // These methods encapsulate the ownership / tenant / state /
    // version / expiry rules that previously lived inline in the
    // application services. Application services now coordinate
    // and delegate the decision to the aggregate.

    /**
     * Asserts that the supplied actor is the original requester AND
     * that the supplied empresa owns the proposal.
     *
     * <p>Checks the actor identity first (cheaper, more common failure
     * path) and only then the empresa. Throws
     * {@link com.ar.crm2.exception.AccionNotOwnedByActorException}
     * (a domain exception that maps to HTTP 403 at the REST boundary)
     * when either check fails.
     *
     * <p>This is the single source of truth for the
     * "only the original requester may operate on this proposal"
     * rule. Application services must call it instead of duplicating
     * the checks inline.
     */
    public void requireOwnedBy(UsuarioId actorUsuarioId, EmpresaId empresaId) {
        DomainAssert.notNull(actorUsuarioId, "actorUsuarioId");
        DomainAssert.notNull(empresaId, "empresaId");
        if (!this.solicitadaPor.equals(actorUsuarioId)) {
            throw com.ar.crm2.exception.AccionNotOwnedByActorException.notRequester(
                    actorUsuarioId.value().toString(),
                    this.id.value().toString()
            );
        }
        if (!this.empresaId.equals(empresaId)) {
            throw com.ar.crm2.exception.AccionNotOwnedByActorException.notRequester(
                    actorUsuarioId.value().toString(),
                    this.id.value().toString()
            );
        }
    }

    /**
     * Asserts that the proposal is in {@link EstadoAccion#PENDING} state
     * for the supplied operation. Used by rejection / expiration /
     * follow-up flows that only act on pending proposals.
     *
     * <p>Throws {@link com.ar.crm2.exception.AccionStateException}
     * otherwise.
     */
    public void requirePending(String operacion) {
        DomainAssert.notBlank(operacion, "operacion");
        if (this.estado != EstadoAccion.PENDING) {
            throw com.ar.crm2.exception.AccionStateException.invalidState(
                    this.id.value().toString(), operacion, this.estado.name()
            );
        }
    }

    /**
     * Asserts that the optimistic-lock {@code version} matches the
     * supplied expected value. Throws
     * {@link com.ar.crm2.exception.AccionVersionMismatchException}
     * otherwise.
     */
    public void requireVersion(int expectedVersion) {
        if (this.version != expectedVersion) {
            throw com.ar.crm2.exception.AccionVersionMismatchException.mismatch(
                    this.id.value().toString(), expectedVersion, this.version
            );
        }
    }

    /**
     * Asserts that the proposal has not expired yet.
     *
     * <p>Only meaningful for PENDING proposals: terminal states
     * never trigger the expiry exception regardless of wall clock.
     * Throws {@link com.ar.crm2.exception.AccionExpiredException}
     * otherwise.
     */
    public void requireNotExpired(LocalDateTime ahora) {
        DomainAssert.notNull(ahora, "ahora");
        if (this.estado == EstadoAccion.PENDING && !ahora.isBefore(this.expiresAt)) {
            throw com.ar.crm2.exception.AccionExpiredException.expired(
                    this.id.value().toString(),
                    this.expiresAt.toString()
            );
        }
    }

    /**
     * Composite confirmation guard. Asserts in this order:
     * <ol>
     *   <li>Estado is PENDING (else {@code AccionStateException}).</li>
     *   <li>Version matches (else {@code AccionVersionMismatchException}).</li>
     *   <li>Proposal is not expired (else {@code AccionExpiredException}).</li>
     * </ol>
     *
     * <p>Returns {@code this} for fluent chaining: callers can write
     * {@code accion.requireConfirmable(...).confirmar(ahora)}.
     *
     * <p>Note: ownership / tenant is NOT part of this guard — call
     * {@link #requireOwnedBy(UsuarioId, EmpresaId)} separately so the
     * audit log can attribute the failure to the right cause.
     */
    public AiAccion requireConfirmable(int expectedVersion, LocalDateTime ahora) {
        requirePending("confirmar");
        requireVersion(expectedVersion);
        requireNotExpired(ahora);
        return this;
    }
}