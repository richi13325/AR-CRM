package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/**
 * Value Object for AiAccion identity.
 *
 * <p>Wraps a UUID that uniquely identifies an AI-suggested CRM action
 * proposal throughout its full lifecycle (PENDING, CONFIRMED, REJECTED,
 * EXPIRED, EXECUTED, FAILED).
 */
public record AiAccionId(UUID value) {

    public AiAccionId {
        DomainAssert.notNull(value, "aiAccionId");
    }

    public static AiAccionId from(UUID value) {
        return new AiAccionId(value);
    }

    public static AiAccionId create() {
        return new AiAccionId(UUID.randomUUID());
    }
}