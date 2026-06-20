package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/** Value Object para la identidad de una NotaTrato. */
public record NotaTratoId(UUID value) {

    public NotaTratoId {
        DomainAssert.notNull(value, "notaTratoId");
    }

    public static NotaTratoId from(UUID value) {
        return new NotaTratoId(value);
    }

    public static NotaTratoId create() {
        return new NotaTratoId(UUID.randomUUID());
    }
}
