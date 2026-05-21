package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/**
 * Value Object for Contacto identity.
 */
public record ContactoId(UUID value) {

    public ContactoId {
        DomainAssert.notNull(value, "contactoId");
    }

    public static ContactoId from(UUID value) {
        return new ContactoId(value);
    }

    public static ContactoId create() {
        return new ContactoId(UUID.randomUUID());
    }
}
