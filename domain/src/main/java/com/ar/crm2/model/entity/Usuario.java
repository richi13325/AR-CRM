package com.ar.crm2.model.entity;

import com.ar.crm2.exception.CambioPasswordNoConfirmadoException;
import com.ar.crm2.model.vo.RolId;
import com.ar.crm2.model.vo.UsuarioId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Rich domain entity for client CRM users.
 *
 * Identity: UsuarioId (wraps UUID).
 * Equality: by id only (not full attribute equality).
 * No public setters — state changes go through business methods that preserve invariants.
 * Constructor is private; use static factory methods create() and reconstitute().
 *
 * Represents ONLY client users — internal superusers are separate (SuperUsuario).
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"passwordHash"})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Usuario {

    @EqualsAndHashCode.Include
    private final UsuarioId id;

    private final String nombre;
    private final String correo;
    private final String passwordHash;
    private final RolId rolId;
    private final LocalDateTime creadoEn;
    private final boolean activo;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new active Usuario for a client.
     * Generates id and creadoEn internally.
     * rolId is mandatory — every client user must have a role within their company.
     */
    public static Usuario create(
        String nombre,
        String correo,
        String passwordHash,
        RolId rolId
    ) {
        return new Usuario(
            UsuarioId.create(),
            DomainAssert.lengthBetween(nombre, "nombre", 1, 100),
            DomainAssert.email(correo, "correo"),
            DomainAssert.lengthBetween(passwordHash, "passwordHash", 1, 255),
            DomainAssert.notNull(rolId, "rolId"),
            LocalDateTime.now(),
            true
        );
    }

    /**
     * Reconstitutes an existing Usuario from persistence.
     */
    public static Usuario reconstitute(
        UsuarioId id,
        String nombre,
        String correo,
        String passwordHash,
        RolId rolId,
        LocalDateTime creadoEn,
        boolean activo
    ) {
        return new Usuario(
            DomainAssert.notNull(id, "id"),
            DomainAssert.lengthBetween(nombre, "nombre", 1, 100),
            DomainAssert.email(correo, "correo"),
            DomainAssert.lengthBetween(passwordHash, "passwordHash", 1, 255),
            DomainAssert.notNull(rolId, "rolId"),
            DomainAssert.notNull(creadoEn, "creadoEn"),
            activo
        );
    }

    public boolean isActivo() {
        return activo;
    }

    // ── Password Change ───────────────────────────────────────────

    /**
     * Changes the password hash for this usuario.
     *
     * @param nuevoPasswordHash the new password hash (required, 1–255 chars)
     * @param codigoConfirmado  true only if email confirmation code was verified externally
     * @return a new Usuario instance with the updated passwordHash, or this instance if idempotent
     * @throws CambioPasswordNoConfirmadoException if codigoConfirmado is false
     */
    public Usuario cambiarPasswordHash(String nuevoPasswordHash, boolean codigoConfirmado) {
        if (!codigoConfirmado) {
            throw new CambioPasswordNoConfirmadoException();
        }
        DomainAssert.lengthBetween(nuevoPasswordHash, "nuevoPasswordHash", 1, 255);
        if (nuevoPasswordHash.equals(this.passwordHash)) {
            return this;
        }
        return new Usuario(id, nombre, correo, nuevoPasswordHash, rolId, creadoEn, activo);
    }
}