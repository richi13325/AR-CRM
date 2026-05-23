package com.ar.crm2.model.entity;

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
@ToString(exclude = {})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Usuario {

    @EqualsAndHashCode.Include
    private final UsuarioId id;

    private final String nombre;
    private final String correo;
    private final RolId rolId;
    private final LocalDateTime creadoEn;
    private final boolean activo;
    private final String keycloakId;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new active Usuario for a client.
     * Generates id and creadoEn internally.
     * rolId is mandatory — every client user must have a role within their company.
     * keycloakId is optional — links this CRM user to a Keycloak identity.
     */
    public static Usuario create(
        String nombre,
        String correo,
        RolId rolId,
        String keycloakId
    ) {
        return new Usuario(
            UsuarioId.create(),
            DomainAssert.lengthBetween(nombre, "nombre", 1, 100),
            DomainAssert.email(correo, "correo"),
            DomainAssert.notNull(rolId, "rolId"),
            LocalDateTime.now(),
            true,
            DomainAssert.optionalLength(keycloakId, 255, "keycloakId")
        );
    }

    /**
     * Reconstitutes an existing Usuario from persistence.
     */
    public static Usuario reconstitute(
        UsuarioId id,
        String nombre,
        String correo,
        RolId rolId,
        LocalDateTime creadoEn,
        boolean activo,
        String keycloakId
    ) {
        return new Usuario(
            DomainAssert.notNull(id, "id"),
            DomainAssert.lengthBetween(nombre, "nombre", 1, 100),
            DomainAssert.email(correo, "correo"),
            DomainAssert.notNull(rolId, "rolId"),
            DomainAssert.notNull(creadoEn, "creadoEn"),
            activo,
            DomainAssert.optionalLength(keycloakId, 255, "keycloakId")
        );
    }

    public boolean isActivo() {
        return activo;
    }

    /**
     * Returns a new Usuario with the given keycloakId.
     * Allows external systems to set the Keycloak linkage without exposing a public setter.
     */
    public Usuario withKeycloakId(String keycloakId) {
        return new Usuario(
            this.id,
            this.nombre,
            this.correo,
            this.rolId,
            this.creadoEn,
            this.activo,
            DomainAssert.optionalLength(keycloakId, 255, "keycloakId")
        );
    }
}