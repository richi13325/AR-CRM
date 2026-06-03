package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

public record AgendaId(UUID value) {

    public AgendaId {
        DomainAssert.notNull(value, "agendaId");
    }

    public static AgendaId from(UUID value) {
        return new AgendaId(value);
    }

    public static AgendaId create() {
        return new AgendaId(UUID.randomUUID());
    }
}
