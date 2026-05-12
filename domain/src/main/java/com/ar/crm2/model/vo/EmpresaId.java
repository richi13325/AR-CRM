package com.ar.crm2.model.vo;

import com.ar.crm2.shared.DomainAssert;

import java.util.UUID;

/**
 * Value Object for Empresa identity.
 */
public record EmpresaId(UUID value) {

    public EmpresaId {
        DomainAssert.notNull(value, "empresaId is mandatory");
    }

    public static EmpresaId from(UUID value) {
        return new EmpresaId(value);
    }

    public static EmpresaId create() {
        return new EmpresaId(UUID.randomUUID());
    }
}
