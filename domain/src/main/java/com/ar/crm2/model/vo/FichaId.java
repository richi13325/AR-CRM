package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/**
 * Value Object for Ficha identity.
 */
public record FichaId(UUID value) {

    public FichaId {
        DomainAssert.notNull(value, "fichaId");
    }

    public static FichaId from(UUID value) {
        return new FichaId(value);
    }

    public static FichaId create() {
        return new FichaId(UUID.randomUUID());
    }
}