package com.ar.crm2.application.trato.port.in;

import com.ar.crm2.application.trato.command.GetTratoByIdCommand;
import com.ar.crm2.model.entity.Trato;

/**
 * Inbound input port for retrieving a specific Trato by id.
 */
public interface GetTratoByIdUseCase {

    /**
     * Retrieves a Trato by the given command.
     *
     * @param command holds the id of the Trato to retrieve
     * @return the requested Trato domain entity
     */
    Trato getById(GetTratoByIdCommand command);
}