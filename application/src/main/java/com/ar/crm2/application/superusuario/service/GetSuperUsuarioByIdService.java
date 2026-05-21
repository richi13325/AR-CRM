package com.ar.crm2.application.superusuario.service;

import com.ar.crm2.application.superusuario.command.GetSuperUsuarioByIdCommand;
import com.ar.crm2.application.superusuario.exception.SuperUsuarioNotFoundException;
import com.ar.crm2.application.superusuario.port.in.GetSuperUsuarioByIdUseCase;
import com.ar.crm2.application.superusuario.port.out.FindSuperUsuarioByIdPort;
import com.ar.crm2.model.entity.SuperUsuario;
import com.ar.crm2.model.vo.SuperUsuarioId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing GetSuperUsuarioByIdUseCase.
 * Retrieves a SuperUsuario by id or throws SuperUsuarioNotFoundException.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class GetSuperUsuarioByIdService implements GetSuperUsuarioByIdUseCase {

    private final FindSuperUsuarioByIdPort findPort;

    @Override
    public SuperUsuario getById(GetSuperUsuarioByIdCommand command) {
        SuperUsuarioId superUsuarioId = SuperUsuarioId.from(command.id());

        return findPort.findById(superUsuarioId)
                .orElseThrow(() -> SuperUsuarioNotFoundException.forId(command.id()));
    }
}