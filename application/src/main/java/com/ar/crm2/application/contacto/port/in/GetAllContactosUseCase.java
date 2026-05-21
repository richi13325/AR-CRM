package com.ar.crm2.application.contacto.port.in;

import com.ar.crm2.model.entity.Contacto;

import java.util.List;

/**
 * Inbound input port for retrieving all Contactos.
 */
public interface GetAllContactosUseCase {

    /**
     * Retrieves all existing Contactos.
     *
     * @return list of all Contactos
     */
    List<Contacto> getAll();
}