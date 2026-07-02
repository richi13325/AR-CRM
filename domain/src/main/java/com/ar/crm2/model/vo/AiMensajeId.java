package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/**
 * Value Object for AiMensaje identity.
 *
 * <p>Wraps a UUID that uniquely identifies a single turn (user, assistant,
 * system or tool) inside an AI conversation. Equality is by UUID value.
 */
public record AiMensajeId(UUID value) {

    public AiMensajeId {
        DomainAssert.notNull(value, "aiMensajeId");
    }

    public static AiMensajeId from(UUID value) {
        return new AiMensajeId(value);
    }

    public static AiMensajeId create() {
        return new AiMensajeId(UUID.randomUUID());
    }
}