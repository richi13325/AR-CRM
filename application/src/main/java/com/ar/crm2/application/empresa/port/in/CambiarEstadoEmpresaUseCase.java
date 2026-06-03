package com.ar.crm2.application.empresa.port.in;

import com.ar.crm2.application.empresa.command.CambiarEstadoEmpresaCommand;
import com.ar.crm2.model.entity.Empresa;

public interface CambiarEstadoEmpresaUseCase {

    /**
     * Changes the relation state of an Empresa.
     *
     * @param command holds the empresaId and target relation state
     * @return the updated domain entity
     */
    Empresa cambiarEstado(CambiarEstadoEmpresaCommand command);
}
