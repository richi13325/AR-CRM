package com.ar.crm2.application.ficha.port.in;

import com.ar.crm2.application.ficha.command.GetFichaByIdCommand;
import com.ar.crm2.model.entity.Ficha;

/**
 * Inbound input port for retrieving a specific Ficha by id.
 */
public interface GetFichaByIdUseCase {

    /**
     * Retrieves a Ficha by the given command.
     *
     * @param command holds the id of the Ficha to retrieve
     * @return the requested Ficha domain entity
     */
    Ficha getById(GetFichaByIdCommand command);
}