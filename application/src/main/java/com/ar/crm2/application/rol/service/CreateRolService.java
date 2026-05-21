package com.ar.crm2.application.rol.service;

import com.ar.crm2.application.rol.command.CreateRolCommand;
import com.ar.crm2.application.rol.port.in.CreateRolUseCase;
import com.ar.crm2.application.rol.port.out.SaveRolPort;
import com.ar.crm2.model.entity.Rol;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing CreateRolUseCase.
 * Orchestrates domain entity creation and outbound persistence via SaveRolPort.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class CreateRolService implements CreateRolUseCase {

    private final SaveRolPort savePort;

    @Override
    public Rol create(CreateRolCommand command) {
        Rol rol = Rol.create(
            command.nombre(),
            command.descripcion()
        );
        return savePort.save(rol);
    }
}