package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/**
 * Value Object for SuperUsuario identity.
 */
public record SuperUsuarioId(UUID value) {

    public SuperUsuarioId {
        DomainAssert.notNull(value, "superUsuarioId");
    }

    public static SuperUsuarioId from(UUID value) {
        return new SuperUsuarioId(value);
    }

    public static SuperUsuarioId create() {
        return new SuperUsuarioId(UUID.randomUUID());
    }
}