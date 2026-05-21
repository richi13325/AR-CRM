package com.ar.crm2.model.entity;

import com.ar.crm2.model.vo.SuperUsuarioId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Rich domain entity for internal superuser (dev/ops team).
 *
 * Identity: SuperUsuarioId (wraps UUID).
 * Equality: by id only (not full attribute equality).
 * No public setters — state changes go through business methods that preserve invariants.
 * Constructor is private; use static factory methods create() and reconstitute().
 *
 * SuperUsuario is SEPARATE from client Usuario — represents internal accounts
 * used to adapt/configure the CRM for clients.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"passwordHash"})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SuperUsuario {

    @EqualsAndHashCode.Include
    private final SuperUsuarioId id;

    private final String correo;
    private final String passwordHash;
    private final LocalDateTime creadoEn;
    private final boolean activo;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new active SuperUsuario.
     * Generates id and creadoEn internally.
     */
    public static SuperUsuario create(
        String correo,
        String passwordHash
    ) {
        return new SuperUsuario(
            SuperUsuarioId.create(),
            DomainAssert.email(correo, "correo"),
            DomainAssert.lengthBetween(passwordHash, "passwordHash", 1, 255),
            LocalDateTime.now(),
            true
        );
    }

    /**
     * Reconstitutes an existing SuperUsuario from persistence.
     */
    public static SuperUsuario reconstitute(
        SuperUsuarioId id,
        String correo,
        String passwordHash,
        LocalDateTime creadoEn,
        boolean activo
    ) {
        return new SuperUsuario(
            DomainAssert.notNull(id, "id"),
            DomainAssert.email(correo, "correo"),
            DomainAssert.lengthBetween(passwordHash, "passwordHash", 1, 255),
            DomainAssert.notNull(creadoEn, "creadoEn"),
            activo
        );
    }

    public boolean isActivo() {
        return activo;
    }
}