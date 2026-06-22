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
@ToString(exclude = {})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SuperUsuario {

    @EqualsAndHashCode.Include
    private final SuperUsuarioId id;

    private final String correo;
    private final LocalDateTime creadoEn;
    private final boolean activo;
    private final String keycloakId;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new active SuperUsuario.
     * Generates id and creadoEn internally.
     * keycloakId is optional — links this superuser to a Keycloak identity.
     */
    public static SuperUsuario create(
        String correo,
        String keycloakId
    ) {
        DomainAssert.email(correo, "correo");

        String normalizedKeycloakId = (keycloakId == null || keycloakId.isBlank())
            ? null
            : keycloakId.trim();
        DomainAssert.optionalLength(normalizedKeycloakId, 255, "keycloakId");

        return new SuperUsuario(
            SuperUsuarioId.create(),
            correo.trim(),
            LocalDateTime.now(),
            true,
            normalizedKeycloakId
        );
    }

    /**
     * Reconstitutes an existing SuperUsuario from persistence.
     */
    public static SuperUsuario reconstitute(
        SuperUsuarioId id,
        String correo,
        LocalDateTime creadoEn,
        boolean activo,
        String keycloakId
    ) {
        DomainAssert.notNull(id, "id");
        DomainAssert.notNull(creadoEn, "creadoEn");
        DomainAssert.email(correo, "correo");

        String normalizedKeycloakId = (keycloakId == null || keycloakId.isBlank())
            ? null
            : keycloakId.trim();
        DomainAssert.optionalLength(normalizedKeycloakId, 255, "keycloakId");

        return new SuperUsuario(
            id,
            correo.trim(),
            creadoEn,
            activo,
            normalizedKeycloakId
        );
    }

    public boolean isActivo() {
        return activo;
    }

    /**
     * Returns a new SuperUsuario with the given keycloakId.
     * Allows external systems to set the Keycloak linkage without exposing a public setter.
     */
    public SuperUsuario withKeycloakId(String keycloakId) {
        String normalizedKeycloakId = (keycloakId == null || keycloakId.isBlank())
            ? null
            : keycloakId.trim();
        DomainAssert.optionalLength(normalizedKeycloakId, 255, "keycloakId");
        return new SuperUsuario(
            this.id,
            this.correo,
            this.creadoEn,
            this.activo,
            normalizedKeycloakId
        );
    }

    /**
     * Returns a new SuperUsuario with the given active flag.
     * Used by application services when synchronizing state to Keycloak.
     */
    public SuperUsuario withActivo(boolean activo) {
        return new SuperUsuario(
            this.id,
            this.correo,
            this.creadoEn,
            activo,
            this.keycloakId
        );
    }
}
