package com.ar.crm2.whatsapp.domain.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

public record MensajeId(UUID value) {

    public MensajeId {
        DomainAssert.notNull(value, "mensajeId");
    }

    public static MensajeId from(UUID value) {
        return new MensajeId(value);
    }

    public static MensajeId create() {
        return new MensajeId(UUID.randomUUID());
    }
}
