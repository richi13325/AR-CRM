package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/**
 * Value Object for AiResumenContexto identity.
 *
 * <p>Wraps a UUID that uniquely identifies a context summary record
 * scoped to (actorUsuarioId, empresaId, waConversacionId, contactoId?).
 * Equality is by UUID value.
 */
public record AiResumenContextoId(UUID value) {

    public AiResumenContextoId {
        DomainAssert.notNull(value, "aiResumenContextoId");
    }

    public static AiResumenContextoId from(UUID value) {
        return new AiResumenContextoId(value);
    }

    public static AiResumenContextoId create() {
        return new AiResumenContextoId(UUID.randomUUID());
    }
}