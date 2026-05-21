package com.ar.crm2.application.usuario.service;

import com.ar.crm2.application.usuario.command.CreateUsuarioCommand;
import com.ar.crm2.application.usuario.port.in.CreateUsuarioUseCase;
import com.ar.crm2.application.usuario.port.out.SaveUsuarioPort;
import com.ar.crm2.model.entity.Usuario;
import com.ar.crm2.model.vo.RolId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing CreateUsuarioUseCase.
 * Orchestrates domain entity creation and outbound persistence via SaveUsuarioPort.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class CreateUsuarioService implements CreateUsuarioUseCase {

    private final SaveUsuarioPort savePort;

    @Override
    public Usuario create(CreateUsuarioCommand command) {
        Usuario usuario = Usuario.create(
            command.nombre(),
            command.correo(),
            command.passwordHash(),
            RolId.from(command.rolId())
        );
        return savePort.save(usuario);
    }
}