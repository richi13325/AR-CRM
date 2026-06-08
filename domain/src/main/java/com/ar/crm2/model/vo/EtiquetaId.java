package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/**
 * Value Object for Etiqueta identity.
 */
public record EtiquetaId(UUID value) {

    public EtiquetaId {
        DomainAssert.notNull(value, "etiquetaId");
    }

    public static EtiquetaId from(UUID value) {
        return new EtiquetaId(value);
    }

    public static EtiquetaId create() {
        return new EtiquetaId(UUID.randomUUID());
    }
}
