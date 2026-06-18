package com.ar.crm2.whatsapp.domain.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

public record GrupoId(UUID value) {

    public GrupoId {
        DomainAssert.notNull(value, "grupoId");
    }

    public static GrupoId from(UUID value) {
        return new GrupoId(value);
    }

    public static GrupoId create() {
        return new GrupoId(UUID.randomUUID());
    }
}
