package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/**
 * Value Object for Usuario identity.
 */
public record UsuarioId(UUID value) {

    public UsuarioId {
        DomainAssert.notNull(value, "usuarioId");
    }

    public static UsuarioId from(UUID value) {
        return new UsuarioId(value);
    }

    public static UsuarioId create() {
        return new UsuarioId(UUID.randomUUID());
    }
}
