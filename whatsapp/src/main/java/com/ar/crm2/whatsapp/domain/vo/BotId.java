package com.ar.crm2.whatsapp.domain.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

public record BotId(UUID value) {

    public BotId {
        DomainAssert.notNull(value, "botId");
    }

    public static BotId from(UUID value) {
        return new BotId(value);
    }

    public static BotId create() {
        return new BotId(UUID.randomUUID());
    }
}
