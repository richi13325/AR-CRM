package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/**
 * Value Object for Columna identity.
 */
public record ColumnaId(UUID value) {

    public ColumnaId {
        DomainAssert.notNull(value, "columnaId is mandatory");
    }

    public static ColumnaId from(UUID value) {
        return new ColumnaId(value);
    }

    public static ColumnaId create() {
        return new ColumnaId(UUID.randomUUID());
    }
}