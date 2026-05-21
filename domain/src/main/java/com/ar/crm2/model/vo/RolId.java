package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/**
 * Value Object for Rol identity.
 */
public record RolId(UUID value) {

    public RolId {
        DomainAssert.notNull(value, "rolId");
    }

    public static RolId from(UUID value) {
        return new RolId(value);
    }

    public static RolId create() {
        return new RolId(UUID.randomUUID());
    }
}