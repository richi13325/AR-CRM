package com.ar.crm2.model.entity.ia;

import com.ar.crm2.model.vo.AiConversacionId;
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
 * Rich domain entity representing an AI assistant session.
 *
 * <p>Aggregates all AI turns (AiMensaje), the latest summary
 * (AiResumenContexto) and atomic memories (AiMemoria) for a given
 * scope: requester user, owning company, source WhatsApp conversation
 * and optional contact/customer.
 *
 * <p>Identity: {@link AiConversacionId} (wraps UUID).
 * Equality: by id only.
 * No public setters — state changes go through business methods.
 * Constructor is private; use static factory methods {@link #crear}
 * and {@link #reconstitute}.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiConversacion {

    @EqualsAndHashCode.Include
    private final AiConversacionId id;

    private final EmpresaId empresaId;
    private final UsuarioId actorUsuarioId;
    private final String waConversacionId;
    private final ContactoId contactoId;
    private final boolean archivada;
    private final LocalDateTime creadoEn;
    private final LocalDateTime actualizadoEn;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new AI conversation in non-archived state.
     *
     * <p>Tenant ownership is encoded as the {@code empresaId}; the
     * application layer is responsible for resolving it from
     * {@code FindEmpresasByCreadorPort} before invoking this factory.
     *
     * @param contactoId optional contact scope; nullable
     */
    public static AiConversacion crear(
            EmpresaId empresaId,
            UsuarioId actorUsuarioId,
            String waConversacionId,
            ContactoId contactoId,
            LocalDateTime ahora
    ) {
        DomainAssert.notNull(empresaId, "empresaId");
        DomainAssert.notNull(actorUsuarioId, "actorUsuarioId");
        DomainAssert.notBlank(waConversacionId, "waConversacionId");

        return new AiConversacion(
                AiConversacionId.create(),
                empresaId,
                actorUsuarioId,
                waConversacionId.trim(),
                contactoId,
                false,
                ahora,
                ahora
        );
    }

    /**
     * Reconstitutes an existing AiConversacion from persistence.
     * No state-machine validation — the caller is the persistence layer
     * which preserves the same invariants on write.
     */
    public static AiConversacion reconstitute(
            AiConversacionId id,
            EmpresaId empresaId,
            UsuarioId actorUsuarioId,
            String waConversacionId,
            ContactoId contactoId,
            boolean archivada,
            LocalDateTime creadoEn,
            LocalDateTime actualizadoEn
    ) {
        DomainAssert.notNull(id, "id");
        DomainAssert.notNull(empresaId, "empresaId");
        DomainAssert.notNull(actorUsuarioId, "actorUsuarioId");
        DomainAssert.notBlank(waConversacionId, "waConversacionId");
        DomainAssert.notNull(creadoEn, "creadoEn");

        return new AiConversacion(
                id, empresaId, actorUsuarioId, waConversacionId,
                contactoId, archivada, creadoEn, actualizadoEn
        );
    }

    // ── State transitions ─────────────────────────────────────────

    /**
     * Archives the conversation. Idempotent — re-archiving returns the
     * same instance.
     */
    public AiConversacion archivar(LocalDateTime ahora) {
        DomainAssert.notNull(ahora, "ahora");
        if (this.archivada) {
            return this;
        }
        return new AiConversacion(
                id, empresaId, actorUsuarioId, waConversacionId,
                contactoId, true, creadoEn, ahora
        );
    }

    // ── Domain queries ────────────────────────────────────────────

    /** True when the conversation was started by the supplied actor. */
    public boolean perteneceA(UsuarioId usuarioId) {
        DomainAssert.notNull(usuarioId, "usuarioId");
        return this.actorUsuarioId.equals(usuarioId);
    }

    /** True when the conversation belongs to the supplied company. */
    public boolean perteneceA(EmpresaId empresaId) {
        DomainAssert.notNull(empresaId, "empresaId");
        return this.empresaId.equals(empresaId);
    }

    /** True when the conversation is scoped to the supplied WhatsApp conversation. */
    public boolean scopeEs(String waConversacionId) {
        DomainAssert.notBlank(waConversacionId, "waConversacionId");
        return this.waConversacionId.equals(waConversacionId.trim());
    }

    /**
     * Asserts that the supplied actor is the original starter of the
     * conversation AND that the supplied empresa owns it.
     *
     * <p>Checks actor identity first (cheaper, more common failure
     * path) and only then the empresa. Throws
     * {@link com.ar.crm2.exception.ConversacionAsistenteNotOwnedByActorException}
     * (a domain exception that maps to HTTP 403 at the REST boundary)
     * otherwise.
     *
     * <p>Single source of truth for the "only the original starter
     * may read or follow up on this conversation" rule. Application
     * services must call this instead of duplicating the checks
     * inline.
     */
    public void requireOwnedBy(UsuarioId actorUsuarioId, EmpresaId empresaId) {
        DomainAssert.notNull(actorUsuarioId, "actorUsuarioId");
        DomainAssert.notNull(empresaId, "empresaId");
        if (!this.actorUsuarioId.equals(actorUsuarioId)) {
            throw com.ar.crm2.exception.ConversacionAsistenteNotOwnedByActorException.notOwner(
                    actorUsuarioId.value().toString(),
                    this.id.value().toString()
            );
        }
        if (!this.empresaId.equals(empresaId)) {
            throw com.ar.crm2.exception.ConversacionAsistenteNotOwnedByActorException.tenantMismatch(
                    actorUsuarioId.value().toString(),
                    this.id.value().toString()
            );
        }
    }
}