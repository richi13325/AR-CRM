package com.ar.crm2.application.usuario.service;

import com.ar.crm2.application.identity.port.out.IdentityProviderUserPort;
import com.ar.crm2.application.usuario.command.DeleteUsuarioCommand;
import com.ar.crm2.application.usuario.exception.UsuarioNotFoundException;
import com.ar.crm2.application.usuario.port.in.DeleteUsuarioUseCase;
import com.ar.crm2.application.usuario.port.out.DeleteUsuarioByIdPort;
import com.ar.crm2.application.usuario.port.out.FindUsuarioByIdPort;
import com.ar.crm2.model.vo.UsuarioId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing DeleteUsuarioUseCase.
 * Validates existence, disables Keycloak user, then deletes local record.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class DeleteUsuarioService implements DeleteUsuarioUseCase {

    private final FindUsuarioByIdPort findPort;
    private final DeleteUsuarioByIdPort deletePort;
    private final IdentityProviderUserPort identityPort;

    @Override
    public void delete(DeleteUsuarioCommand command) {
        UsuarioId usuarioId = UsuarioId.from(command.id());

        // Verify usuario exists and get keycloakId for Keycloak disable
        var existing = findPort.findById(usuarioId)
                .orElseThrow(() -> UsuarioNotFoundException.forId(command.id()));

        // Disable in Keycloak before local delete
        if (existing.getKeycloakId() != null) {
            identityPort.setEnabled(existing.getKeycloakId(), false);
        }

        deletePort.deleteById(usuarioId);
    }
}