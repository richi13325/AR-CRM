package com.ar.crm2.application.empresa.port.in;

import com.ar.crm2.application.empresa.command.CreateEmpresaCommand;
import com.ar.crm2.model.entity.Empresa;

/**
 * Inbound input port for creating a new Empresa.
 * UseCase suffix per project rules: inbound contracts live in port/in package.
 */
public interface CreateEmpresaUseCase {

    /**
     * Creates a new Empresa from the given command.
     *
     * @param command holds the required fields for creation
     * @return the created domain entity
     */
    Empresa create(CreateEmpresaCommand command);
}