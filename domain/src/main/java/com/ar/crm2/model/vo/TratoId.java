package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/**
 * Value Object for Trato identity.
 */
public record TratoId(UUID value) {

    public TratoId {
        DomainAssert.notNull(value, "tratoId");
    }

    public static TratoId from(UUID value) {
        return new TratoId(value);
    }

    public static TratoId create() {
        return new TratoId(UUID.randomUUID());
    }
}