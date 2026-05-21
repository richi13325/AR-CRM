package com.ar.crm2.application.contacto.port.out;

import com.ar.crm2.model.vo.ContactoId;

/**
 * Granular outbound port for deleting a Contacto by its id.
 * Single-method contract per project rules.
 */
public interface DeleteContactoByIdPort {

    /**
     * Deletes a Contacto by its id.
     *
     * @param id the ContactoId to delete
     */
    void deleteById(ContactoId id);
}