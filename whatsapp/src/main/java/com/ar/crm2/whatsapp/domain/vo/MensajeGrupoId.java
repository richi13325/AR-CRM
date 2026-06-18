package com.ar.crm2.whatsapp.domain.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

public record MensajeGrupoId(UUID value) {

    public MensajeGrupoId {
        DomainAssert.notNull(value, "mensajeGrupoId");
    }

    public static MensajeGrupoId from(UUID value) {
        return new MensajeGrupoId(value);
    }

    public static MensajeGrupoId create() {
        return new MensajeGrupoId(UUID.randomUUID());
    }
}
