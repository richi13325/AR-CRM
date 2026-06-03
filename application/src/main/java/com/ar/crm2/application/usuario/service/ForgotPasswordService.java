package com.ar.crm2.application.usuario.service;

import com.ar.crm2.application.identity.port.out.IdentityProviderUserPort;
import com.ar.crm2.application.usuario.command.ForgotPasswordCommand;
import com.ar.crm2.application.usuario.port.in.ForgotPasswordUseCase;
import com.ar.crm2.application.usuario.port.out.FindUsuarioByCorreoPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ForgotPasswordService implements ForgotPasswordUseCase {

    private final FindUsuarioByCorreoPort findByCorreoPort;
    private final IdentityProviderUserPort identityPort;

    @Override
    public void requestReset(ForgotPasswordCommand command) {
        findByCorreoPort.findByCorreo(command.correo())
                .filter(u -> u.getKeycloakId() != null)
                .ifPresent(usuario -> {
                    try {
                        identityPort.sendUpdatePasswordEmail(usuario.getKeycloakId());
                    } catch (RuntimeException ignored) {
                        // Keep forgot-password response generic to avoid account enumeration.
                    }
                });
    }
}
