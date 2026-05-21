package com.ar.crm2.application.empresa.service;

import com.ar.crm2.application.empresa.command.CreateEmpresaCommand;
import com.ar.crm2.application.empresa.port.out.SaveEmpresaPort;
import com.ar.crm2.application.empresa.port.in.CreateEmpresaUseCase;
import com.ar.crm2.model.entity.Empresa;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing CreateEmpresaUseCase.
 * Orchestrates domain entity creation and outbound persistence via SaveEmpresaPort.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class CreateEmpresaService implements CreateEmpresaUseCase {

    private final SaveEmpresaPort savePort;

    @Override
    public Empresa create(CreateEmpresaCommand command) {
        Empresa empresa = Empresa.create(
            command.nombre(),
            command.sector(),
            command.telefono(),
            command.paginaWeb(),
            command.facebook(),
            command.instagram(),
            command.twitter(),
            command.estadoRelacion(),
            command.responsableId() != null ? UsuarioId.from(command.responsableId()) : null,
            command.creadoPor() != null ? UsuarioId.from(command.creadoPor()) : null,
            command.notas()
        );
        return savePort.save(empresa);
    }
}