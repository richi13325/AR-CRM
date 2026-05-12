package com.ar.crm2.application.empresa.port.in;

import com.ar.crm2.application.empresa.command.CreateEmpresaCommand;
import com.ar.crm2.model.entity.Empresa;

/**
 * Input port for creating a new Empresa.
 */
public interface CreateEmpresaPort {

    /**
     * Creates a new Empresa from the given command.
     *
     * @param command holds the required nombre
     * @return the created domain entity
     */
    Empresa create(CreateEmpresaCommand command);
}