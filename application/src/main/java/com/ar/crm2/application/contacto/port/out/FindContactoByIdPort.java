package com.ar.crm2.application.contacto.port.out;

import com.ar.crm2.model.entity.Contacto;
import com.ar.crm2.model.vo.ContactoId;

import java.util.Optional;

/**
 * Granular outbound port for finding a Contacto by its id.
 * Single-method contract per project rules.
 */
public interface FindContactoByIdPort {

    /**
     * Finds a Contacto by its id.
     *
     * @param id the ContactoId to search for
     * @return an Optional containing the Contacto if found, empty otherwise
     */
    Optional<Contacto> findById(ContactoId id);
}