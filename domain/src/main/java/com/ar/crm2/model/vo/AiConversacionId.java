package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/**
 * Value Object for AiConversacion identity.
 *
 * <p>Wraps a UUID that uniquely identifies an AI assistant session
 * scoped to (actorUsuarioId, empresaId, waConversacionId, contactoId?).
 * Equality is by UUID value.
 */
public record AiConversacionId(UUID value) {

    public AiConversacionId {
        DomainAssert.notNull(value, "aiConversacionId");
    }

    public static AiConversacionId from(UUID value) {
        return new AiConversacionId(value);
    }

    public static AiConversacionId create() {
        return new AiConversacionId(UUID.randomUUID());
    }
}