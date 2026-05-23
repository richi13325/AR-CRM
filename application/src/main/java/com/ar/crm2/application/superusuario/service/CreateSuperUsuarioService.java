package com.ar.crm2.application.superusuario.service;

import com.ar.crm2.application.superusuario.command.CreateSuperUsuarioCommand;
import com.ar.crm2.application.superusuario.port.in.CreateSuperUsuarioUseCase;
import com.ar.crm2.application.superusuario.port.out.SaveSuperUsuarioPort;
import com.ar.crm2.model.entity.SuperUsuario;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing CreateSuperUsuarioUseCase.
 * Orchestrates domain entity creation and outbound persistence via SaveSuperUsuarioPort.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class CreateSuperUsuarioService implements CreateSuperUsuarioUseCase {

    private final SaveSuperUsuarioPort savePort;

    @Override
    public SuperUsuario create(CreateSuperUsuarioCommand command) {
        SuperUsuario superUsuario = SuperUsuario.create(
            command.correo(),
            command.passwordHash(),
            command.keycloakId()
        );
        return savePort.save(superUsuario);
    }
}