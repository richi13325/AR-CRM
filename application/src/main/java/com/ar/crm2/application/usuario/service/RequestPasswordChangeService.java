package com.ar.crm2.application.usuario.service;

import com.ar.crm2.application.identity.port.out.SendIdentityUpdatePasswordEmailPort;
import com.ar.crm2.application.usuario.command.RequestPasswordChangeCommand;
import com.ar.crm2.application.usuario.exception.UsuarioNotFoundException;
import com.ar.crm2.application.usuario.port.in.RequestPasswordChangeUseCase;
import com.ar.crm2.application.usuario.port.out.FindUsuarioByIdPort;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RequestPasswordChangeService implements RequestPasswordChangeUseCase {

    private final FindUsuarioByIdPort findPort;
    private final SendIdentityUpdatePasswordEmailPort sendUpdatePasswordEmailPort;

    @Override
    public void requestChange(RequestPasswordChangeCommand command) {
        UsuarioId usuarioId = UsuarioId.from(command.usuarioId());

        var usuario = findPort.findById(usuarioId)
                .orElseThrow(() -> UsuarioNotFoundException.forId(command.usuarioId()));

        if (usuario.getKeycloakId() != null) {
            sendUpdatePasswordEmailPort.sendUpdatePasswordEmail(usuario.getKeycloakId());
        }
    }
}
