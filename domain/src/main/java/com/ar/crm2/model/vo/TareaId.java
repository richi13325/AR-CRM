package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/**
 * Value Object for Tarea identity.
 */
public record TareaId(UUID value) {

    public TareaId {
        DomainAssert.notNull(value, "tareaId");
    }

    public static TareaId from(UUID value) {
        return new TareaId(value);
    }

    public static TareaId create() {
        return new TareaId(UUID.randomUUID());
    }
}