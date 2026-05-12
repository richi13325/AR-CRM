package com.ar.crm2.application.empresa.service;

import com.ar.crm2.application.empresa.command.CreateEmpresaCommand;
import com.ar.crm2.application.empresa.port.in.CreateEmpresaPort;
import com.ar.crm2.application.empresa.port.in.GetEmpresasPort;
import com.ar.crm2.application.empresa.port.out.EmpresaRepositoryPort;
import com.ar.crm2.model.entity.Empresa;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Application service implementing Empresa input ports.
 * Orchestrates domain operations and outbound port calls.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class EmpresaService implements CreateEmpresaPort, GetEmpresasPort {

    private final EmpresaRepositoryPort repositoryPort;

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
        return repositoryPort.save(empresa);
    }

    @Override
    public List<Empresa> getAll() {
        return repositoryPort.findAll();
    }
}