package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.Contacto;
import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;

import java.util.Optional;

/**
 * Outbound port for reading Contacto aggregates for the AI use cases.
 *
 * <p>Implemented in infrastructure by the canonical
 * {@code ContactoRepositoryAdapter}, which already satisfies the
 * signature contract for both the CRM read port and the AI read
 * port (see design.md §"Module Boundaries"). No bridge adapter is
 * needed.
 *
 * <p><b>Tenant scoping.</b> the {@link #findByEmpresaIdAndTelefono}
 * lookup explicitly takes an {@link EmpresaId} so the AI tool
 * adapter can pass the trusted tenant id (resolved from
 * {@code AiToolContextPort}) instead of trusting the model payload.
 * Tenant authorization for the AI flows is enforced one level up —
 * in the AI tool / application service — this port stays
 * tenant-blind.
 */
public interface ContactoLecturaPort {

    Optional<Contacto> findById(ContactoId id);

    /**
     * Resolves a Contacto by the given (empresaId, telefono) tuple.
     * Used by the AI tool that needs to look up an existing
     * customer before proposing {@code CREATE_CONTACTO}.
     *
     * @param empresaId trusted tenant id from the AI tool context
     * @param telefono  phone number in canonical CRM form (the
     *                  caller normalizes; the port is a pass-through)
     * @return the matching Contacto, or empty when none exists
     */
    Optional<Contacto> findByEmpresaIdAndTelefono(EmpresaId empresaId, String telefono);
}