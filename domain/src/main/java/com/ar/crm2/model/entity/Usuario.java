package com.ar.crm2.model.entity;

import com.ar.crm2.model.enums.RolSistema;
import com.ar.crm2.model.vo.UsuarioId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Rich domain entity for Usuario.
 *
 * Identity: UsuarioId (wraps UUID).
 * Equality: by id only (not full attribute equality).
 * No public setters — state changes go through business methods that preserve invariants.
 * Constructor is private; use static factory methods create() and reconstitute().
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
    private final RolSistema rolSistema;
    private final String rolEmpresa;
    private final LocalDateTime creadoEn;
    private final boolean activo;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new active Usuario.
     * Generates id and creadoEn internally.
     */
    public static Usuario create(
        String nombre,
        String correo,
        String passwordHash,
        RolSistema rolSistema,
        String rolEmpresa
    ) {
        return new Usuario(
            UsuarioId.create(),
            DomainAssert.lengthBetween(nombre, 1, 100, "nombre must be 1-100 chars"),
            DomainAssert.lengthBetween(correo, 1, 150, "correo must be 1-150 chars"),
            DomainAssert.lengthBetween(passwordHash, 1, 255, "passwordHash must be 1-255 chars"),
            DomainAssert.notNull(rolSistema, "rolSistema is mandatory"),
            DomainAssert.lengthBetween(rolEmpresa, 1, 80, "rolEmpresa must be 1-80 chars"),
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
        RolSistema rolSistema,
        String rolEmpresa,
        LocalDateTime creadoEn,
        boolean activo
    ) {
        return new Usuario(
            DomainAssert.notNull(id, "id is mandatory"),
            DomainAssert.lengthBetween(nombre, 1, 100, "nombre must be 1-100 chars"),
            DomainAssert.lengthBetween(correo, 1, 150, "correo must be 1-150 chars"),
            DomainAssert.lengthBetween(passwordHash, 1, 255, "passwordHash must be 1-255 chars"),
            DomainAssert.notNull(rolSistema, "rolSistema is mandatory"),
            DomainAssert.lengthBetween(rolEmpresa, 1, 80, "rolEmpresa must be 1-80 chars"),
            DomainAssert.notNull(creadoEn, "creadoEn is mandatory"),
            activo
        );
    }

    public boolean isActivo() {
        return activo;
    }
}
