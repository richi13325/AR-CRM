package com.ar.crm2.application.superusuario.service;

import com.ar.crm2.application.identity.port.out.IdentityProviderUserPort;
import com.ar.crm2.application.superusuario.command.CreateSuperUsuarioCommand;
import com.ar.crm2.application.superusuario.port.in.CreateSuperUsuarioUseCase;
import com.ar.crm2.application.superusuario.port.out.SaveSuperUsuarioPort;
import com.ar.crm2.model.entity.SuperUsuario;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing CreateSuperUsuarioUseCase.
 * Orchestrates domain entity creation, Keycloak provisioning, and persistence.
 * On local save failure after Keycloak creation, compensates by deleting the Keycloak user.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class CreateSuperUsuarioService implements CreateSuperUsuarioUseCase {

    private final SaveSuperUsuarioPort savePort;
    private final IdentityProviderUserPort identityPort;

    @Override
    public SuperUsuario create(CreateSuperUsuarioCommand command) {
        // Provision Keycloak user first
        var provisioned = identityPort.provision(
            command.correo(),
            command.initialPassword(),
            true // enabled
        );

        SuperUsuario superUsuario;
        try {
            // Build entity with keycloakId from provisioning
            superUsuario = SuperUsuario.create(
                command.correo(),
                provisioned.keycloakId()
            );
            return savePort.save(superUsuario);
        } catch (RuntimeException saveFailure) {
            // Compensate: delete the Keycloak user if local save fails
            try {
                identityPort.delete(provisioned.keycloakId());
            } catch (RuntimeException compensationFailure) {
                // Log but don't mask the original save failure
                // The Keycloak user may remain orphaned — operator must review
            }
            throw saveFailure;
        }
    }
}