package com.ar.crm2.application.usuario.service;

import com.ar.crm2.application.identity.port.out.IdentityProviderUserPort;
import com.ar.crm2.application.usuario.command.CreateUsuarioCommand;
import com.ar.crm2.application.usuario.port.in.CreateUsuarioUseCase;
import com.ar.crm2.application.usuario.port.out.SaveUsuarioPort;
import com.ar.crm2.model.entity.Usuario;
import com.ar.crm2.model.vo.RolId;
import lombok.RequiredArgsConstructor;

/**
 * Application service implementing CreateUsuarioUseCase.
 * Orchestrates domain entity creation, Keycloak provisioning, and persistence.
 * On local save failure after Keycloak creation, compensates by deleting the Keycloak user.
 * No Spring annotations — constructor injection via Lombok.
 */
@RequiredArgsConstructor
public class CreateUsuarioService implements CreateUsuarioUseCase {

    private final SaveUsuarioPort savePort;
    private final IdentityProviderUserPort identityPort;

    @Override
    public Usuario create(CreateUsuarioCommand command) {
        // Provision Keycloak user first
        var provisioned = identityPort.provision(
            command.correo(),
            command.initialPassword(),
            true // enabled
        );

        Usuario usuario;
        try {
            // Build entity with keycloakId from provisioning
            usuario = Usuario.create(
                command.nombre(),
                command.correo(),
                RolId.from(command.rolId()),
                provisioned.keycloakId()
            );
            return savePort.save(usuario);
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