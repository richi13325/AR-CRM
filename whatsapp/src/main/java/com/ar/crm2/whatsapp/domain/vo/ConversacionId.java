package com.ar.crm2.whatsapp.domain.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

public record ConversacionId(UUID value) {

    public ConversacionId {
        DomainAssert.notNull(value, "conversacionId");
    }

    public static ConversacionId from(UUID value) {
        return new ConversacionId(value);
    }

    public static ConversacionId create() {
        return new ConversacionId(UUID.randomUUID());
    }
}
