package com.ar.crm2.application.empresa.port.in;

import com.ar.crm2.application.empresa.command.DeleteEmpresaCommand;

/**
 * Inbound input port for deleting an existing Empresa.
 */
public interface DeleteEmpresaUseCase {

    /**
     * Deletes an existing Empresa by the given command.
     *
     * @param command holds the id of the Empresa to delete
     */
    void delete(DeleteEmpresaCommand command);
}