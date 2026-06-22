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
        DomainAssert.notNull(rolId, "rolId");
        DomainAssert.lengthBetween(nombre, "nombre", 1, 100);
        DomainAssert.email(correo, "correo");

        String normalizedKeycloakId = (keycloakId == null || keycloakId.isBlank())
            ? null
            : keycloakId.trim();
        DomainAssert.optionalLength(normalizedKeycloakId, 255, "keycloakId");

        return new Usuario(
            UsuarioId.create(),
            nombre.trim(),
            correo.trim(),
            rolId,
            LocalDateTime.now(),
            true,
            normalizedKeycloakId
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
        DomainAssert.notNull(id, "id");
        DomainAssert.notNull(rolId, "rolId");
        DomainAssert.notNull(creadoEn, "creadoEn");
        DomainAssert.lengthBetween(nombre, "nombre", 1, 100);
        DomainAssert.email(correo, "correo");

        String normalizedKeycloakId = (keycloakId == null || keycloakId.isBlank())
            ? null
            : keycloakId.trim();
        DomainAssert.optionalLength(normalizedKeycloakId, 255, "keycloakId");

        return new Usuario(
            id,
            nombre.trim(),
            correo.trim(),
            rolId,
            creadoEn,
            activo,
            normalizedKeycloakId
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
        String normalizedKeycloakId = (keycloakId == null || keycloakId.isBlank())
            ? null
            : keycloakId.trim();
        DomainAssert.optionalLength(normalizedKeycloakId, 255, "keycloakId");
        return new Usuario(
            this.id,
            this.nombre,
            this.correo,
            this.rolId,
            this.creadoEn,
            this.activo,
            normalizedKeycloakId
        );
    }

    /**
     * Returns a new Usuario with the given active flag.
     * Used by application services when synchronizing state to Keycloak.
     */
    public Usuario withActivo(boolean activo) {
        return new Usuario(
            this.id,
            this.nombre,
            this.correo,
            this.rolId,
            this.creadoEn,
            activo,
            this.keycloakId
        );
    }
}
