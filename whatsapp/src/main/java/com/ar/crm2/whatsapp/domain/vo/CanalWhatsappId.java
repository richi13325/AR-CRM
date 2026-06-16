package com.ar.crm2.whatsapp.domain.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

public record CanalWhatsappId(UUID value) {

    public CanalWhatsappId {
        DomainAssert.notNull(value, "canalWhatsappId");
    }

    public static CanalWhatsappId from(UUID value) {
        return new CanalWhatsappId(value);
    }

    public static CanalWhatsappId create() {
        return new CanalWhatsappId(UUID.randomUUID());
    }
}
