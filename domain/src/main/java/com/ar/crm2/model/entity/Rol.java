package com.ar.crm2.model.entity;

import com.ar.crm2.model.vo.RolId;
import com.ar.crm2.shared.DomainAssert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Rich domain entity for Rol.
 *
 * Identity: RolId (wraps UUID).
 * Equality: by id only (not full attribute equality).
 * No public setters — state changes go through business methods that preserve invariants.
 * Constructor is private; use static factory methods create() and reconstitute().
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Rol {

    @EqualsAndHashCode.Include
    private final RolId id;
    private final String nombre;
    private final String descripcion;
    private final boolean activo;

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Creates a new active Rol.
     * Generates id and sets activo=true.
     */
    public static Rol create(
        String nombre,
        String descripcion
    ) {
        return Rol.builder()
            .id(RolId.create())
            .nombre(DomainAssert.lengthBetween(nombre, "nombre", 1, 80))
            .descripcion(descripcion)
            .activo(true)
            .build();
    }

    /**
     * Reconstitutes an existing Rol from persistence.
     */
    public static Rol reconstitute(
        RolId id,
        String nombre,
        String descripcion,
        boolean activo
    ) {
        return Rol.builder()
            .id(DomainAssert.notNull(id, "id"))
            .nombre(DomainAssert.lengthBetween(nombre, "nombre", 1, 80))
            .descripcion(descripcion)
            .activo(activo)
            .build();
    }

    public boolean isActivo() {
        return activo;
    }
}