package com.ar.crm2.application.empresa.port.in;

import com.ar.crm2.application.empresa.command.EditEmpresaCommand;
import com.ar.crm2.model.entity.Empresa;

/**
 * Inbound input port for editing an existing Empresa.
 */
public interface EditEmpresaUseCase {

    /**
     * Updates an existing Empresa with the given command.
     *
     * @param command holds the id of the Empresa to edit and the updated fields
     * @return the updated domain entity
     */
    Empresa edit(EditEmpresaCommand command);
}