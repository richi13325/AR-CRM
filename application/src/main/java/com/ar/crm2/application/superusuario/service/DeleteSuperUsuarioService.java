package com.ar.crm2.application.superusuario.service;

import com.ar.crm2.application.identity.port.out.IdentityProviderUserPort;
import com.ar.crm2.application.superusuario.command.DeleteSuperUsuarioCommand;
import com.ar.crm2.application.superusuario.exception.SuperUsuarioNotFoundException;
import com.ar.crm2.application.superusuario.port.in.DeleteSuperUsuarioUseCase;
import com.ar.crm2.application.superusuario.port.out.DeleteSuperUsuarioByIdPort;
import com.ar.crm2.application.superusuario.port.out.FindSuperUsuarioByIdPort;
import com.ar.crm2.model.vo.SuperUsuarioId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing DeleteSuperUsuarioUseCase.
 * Validates existence, disables Keycloak user, then deletes local record.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class DeleteSuperUsuarioService implements DeleteSuperUsuarioUseCase {

    private final FindSuperUsuarioByIdPort findPort;
    private final DeleteSuperUsuarioByIdPort deletePort;
    private final IdentityProviderUserPort identityPort;

    @Override
    public void delete(DeleteSuperUsuarioCommand command) {
        SuperUsuarioId superUsuarioId = SuperUsuarioId.from(command.id());

        // Verify superUsuario exists and get keycloakId for Keycloak disable
        var existing = findPort.findById(superUsuarioId)
                .orElseThrow(() -> SuperUsuarioNotFoundException.forId(command.id()));

        // Disable in Keycloak before local delete
        if (existing.getKeycloakId() != null) {
            identityPort.setEnabled(existing.getKeycloakId(), false);
        }

        deletePort.deleteById(superUsuarioId);
    }
}