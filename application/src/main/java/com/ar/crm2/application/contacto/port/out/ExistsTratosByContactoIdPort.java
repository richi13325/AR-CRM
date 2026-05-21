package com.ar.crm2.application.contacto.port.out;

import com.ar.crm2.model.vo.ContactoId;

/**
 * Granular outbound port for checking if a Contacto has associated Tratos.
 * Single-method contract per project rules.
 */
public interface ExistsTratosByContactoIdPort {

    /**
     * Checks whether any Tratos are associated with the given Contacto.
     *
     * @param contactoId the ContactoId to check
     * @return true if at least one Trato exists for the Contacto, false otherwise
     */
    boolean existsTratosByContactoId(ContactoId contactoId);
}