package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/**
 * Value Object for Tablero identity.
 */
public record TableroId(UUID value) {

    public TableroId {
        DomainAssert.notNull(value, "tableroId is mandatory");
    }

    public static TableroId from(UUID value) {
        return new TableroId(value);
    }

    public static TableroId create() {
        return new TableroId(UUID.randomUUID());
    }
}
