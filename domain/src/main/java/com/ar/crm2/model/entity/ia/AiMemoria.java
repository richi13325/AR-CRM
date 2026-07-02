package com.ar.crm2.model.entity.ia;

import com.ar.crm2.exception.InvariantViolationException;
import com.ar.crm2.model.enums.OrigenMemoria;
import com.ar.crm2.model.enums.VisibilidadMemoria;
import com.ar.crm2.model.vo.AiMemoriaId;
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
 * Rich domain entity representing an atomic AI memory record.
 *
 * <p>Each record stores ONE reusable fact or decision. Memories are
 * always private to the requester scope
 * {@code (actorUsuarioId, empresaId, waConversacionId | contactoId)};
 * there is NO global or company-wide memory.
 *
 * <p>Identity: {@link AiMemoriaId} (wraps UUID).
 * Equality: by id only.
 * Constructor is private; use static factory methods {@link #crear}
 * and {@link #reconstitute}.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiMemoria {

    @EqualsAndHashCode.Include
    private final AiMemoriaId id;

    private final UsuarioId actorUsuarioId;
    private final EmpresaId empresaId;
    private final String waConversacionId;
    private final ContactoId contactoId;
    private final VisibilidadMemoria visibilidad;
    private final String contenido;
    private final OrigenMemoria origenTipo;
    private final String origenId;
    private final long version;
    private final LocalDateTime creadoEn;
    private final LocalDateTime actualizadoEn;
    private final LocalDateTime expiresAt;
    private final AiMemoriaId supersededBy;
    private final boolean superseded;
    private final boolean expirada;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new active AI memory.
     *
     * <p>Visibility rules (enforced by this factory):
     * <ul>
     *   <li>{@link VisibilidadMemoria#CONVERSACION_SCOPED} requires a non-blank {@code waConversacionId} and a null {@code contactoId}.</li>
     *   <li>{@link VisibilidadMemoria#CONTACTO_SCOPED} requires a non-null {@code contactoId} and a null {@code waConversacionId}.</li>
     * </ul>
     *
     * @param ttl       minutes until the memory expires; must be positive
     * @param expiresAt absolute expiry timestamp; computed by caller from ttl + ahora
     *                  (kept explicit so the application layer can use
     *                  its own clock and the domain stays testable)
     */
    public static AiMemoria crear(
            UsuarioId actorUsuarioId,
            EmpresaId empresaId,
            String waConversacionId,
            ContactoId contactoId,
            VisibilidadMemoria visibilidad,
            String contenido,
            OrigenMemoria origenTipo,
            String origenId,
            LocalDateTime ahora,
            LocalDateTime expiresAt
    ) {
        DomainAssert.notNull(actorUsuarioId, "actorUsuarioId");
        DomainAssert.notNull(empresaId, "empresaId");
        DomainAssert.notNull(visibilidad, "visibilidad");
        DomainAssert.notNull(origenTipo, "origenTipo");
        DomainAssert.lengthBetween(contenido, "contenido", 1, 4000);
        DomainAssert.notNull(ahora, "ahora");
        DomainAssert.notNull(expiresAt, "expiresAt");
        if (!expiresAt.isAfter(ahora)) {
            throw new InvariantViolationException("El campo expiresAt debe ser posterior a ahora.");
        }
        validateScope(visibilidad, waConversacionId, contactoId);

        return new AiMemoria(
                AiMemoriaId.create(),
                actorUsuarioId,
                empresaId,
                waConversacionId,
                contactoId,
                visibilidad,
                contenido.trim(),
                origenTipo,
                origenId,
                1L,
                ahora,
                ahora,
                expiresAt,
                null,
                false,
                false
        );
    }

    /**
     * Reconstitutes an existing AiMemoria from persistence.
     */
    public static AiMemoria reconstitute(
            AiMemoriaId id,
            UsuarioId actorUsuarioId,
            EmpresaId empresaId,
            String waConversacionId,
            ContactoId contactoId,
            VisibilidadMemoria visibilidad,
            String contenido,
            OrigenMemoria origenTipo,
            String origenId,
            long version,
            LocalDateTime creadoEn,
            LocalDateTime actualizadoEn,
            LocalDateTime expiresAt,
            AiMemoriaId supersededBy,
            boolean superseded,
            boolean expirada
    ) {
        DomainAssert.notNull(id, "id");
        DomainAssert.notNull(actorUsuarioId, "actorUsuarioId");
        DomainAssert.notNull(empresaId, "empresaId");
        DomainAssert.notNull(visibilidad, "visibilidad");
        DomainAssert.notNull(creadoEn, "creadoEn");

        return new AiMemoria(
                id, actorUsuarioId, empresaId, waConversacionId, contactoId,
                visibilidad, contenido, origenTipo, origenId, version,
                creadoEn, actualizadoEn, expiresAt,
                supersededBy, superseded, expirada
        );
    }

    // ── State transitions ─────────────────────────────────────────

    /**
     * Marks this memory as superseded by another memory. The replacement
     * id is recorded for traceability. Idempotent on already-superseded.
     */
    public AiMemoria supersede(AiMemoriaId replacement, LocalDateTime ahora) {
        DomainAssert.notNull(replacement, "replacement");
        DomainAssert.notNull(ahora, "ahora");
        if (this.superseded) {
            return this;
        }
        return new AiMemoria(
                id, actorUsuarioId, empresaId, waConversacionId, contactoId,
                visibilidad, contenido, origenTipo, origenId, version + 1,
                creadoEn, ahora, expiresAt,
                replacement, true, expirada
        );
    }

    /**
     * Marks this memory as expired. Idempotent on already-expired.
     */
    public AiMemoria expirar(LocalDateTime ahora) {
        DomainAssert.notNull(ahora, "ahora");
        if (this.expirada) {
            return this;
        }
        return new AiMemoria(
                id, actorUsuarioId, empresaId, waConversacionId, contactoId,
                visibilidad, contenido, origenTipo, origenId, version + 1,
                creadoEn, ahora, expiresAt,
                supersededBy, superseded, true
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

    public boolean estaExpirada(LocalDateTime ahora) {
        DomainAssert.notNull(ahora, "ahora");
        return this.expirada || !ahora.isBefore(this.expiresAt);
    }

    public boolean estaViva(LocalDateTime ahora) {
        return !superseded && !estaExpirada(ahora);
    }

    // ── Internal helpers ──────────────────────────────────────────

    private static void validateScope(
            VisibilidadMemoria visibilidad,
            String waConversacionId,
            ContactoId contactoId
    ) {
        switch (visibilidad) {
            case CONVERSACION_SCOPED -> {
                if (waConversacionId == null || waConversacionId.isBlank()) {
                    throw new InvariantViolationException(
                            "Visibilidad CONVERSACION_SCOPED requiere waConversacionId."
                    );
                }
                if (contactoId != null) {
                    throw new InvariantViolationException(
                            "Visibilidad CONVERSACION_SCOPED no admite contactoId."
                    );
                }
            }
            case CONTACTO_SCOPED -> {
                if (contactoId == null) {
                    throw new InvariantViolationException(
                            "Visibilidad CONTACTO_SCOPED requiere contactoId."
                    );
                }
                if (waConversacionId != null && !waConversacionId.isBlank()) {
                    throw new InvariantViolationException(
                            "Visibilidad CONTACTO_SCOPED no admite waConversacionId."
                    );
                }
            }
        }
    }
}