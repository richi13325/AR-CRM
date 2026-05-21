package com.ar.crm2.application.rol.service;

import com.ar.crm2.application.rol.command.GetRolByIdCommand;
import com.ar.crm2.application.rol.exception.RolNotFoundException;
import com.ar.crm2.application.rol.port.in.GetRolByIdUseCase;
import com.ar.crm2.application.rol.port.out.FindRolByIdPort;
import com.ar.crm2.model.entity.Rol;
import com.ar.crm2.model.vo.RolId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing GetRolByIdUseCase.
 * Loads a Rol by id or throws RolNotFoundException.
 */
@RequiredArgsConstructor
public class GetRolByIdService implements GetRolByIdUseCase {

    private final FindRolByIdPort findPort;

    @Override
    public Rol getById(GetRolByIdCommand command) {
        RolId rolId = RolId.from(command.id());

        return findPort.findById(rolId)
                .orElseThrow(() -> RolNotFoundException.forId(command.id()));
    }
}