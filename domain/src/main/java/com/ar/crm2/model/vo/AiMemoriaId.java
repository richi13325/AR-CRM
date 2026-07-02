package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/**
 * Value Object for AiMemoria identity.
 *
 * <p>Wraps a UUID that uniquely identifies an atomic AI memory record.
 * Memory is private to a (actorUsuarioId, empresaId, waConversacionId?)
 * or (actorUsuarioId, empresaId, contactoId?) scope. Equality is by UUID.
 */
public record AiMemoriaId(UUID value) {

    public AiMemoriaId {
        DomainAssert.notNull(value, "aiMemoriaId");
    }

    public static AiMemoriaId from(UUID value) {
        return new AiMemoriaId(value);
    }

    public static AiMemoriaId create() {
        return new AiMemoriaId(UUID.randomUUID());
    }
}