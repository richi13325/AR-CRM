package com.ar.crm2.application.superusuario.port.out;

import com.ar.crm2.model.entity.SuperUsuario;

import java.util.Optional;

/**
 * Granular outbound port for finding a SuperUsuario by its Keycloak identity.
 * Single-method contract per project rules.
 */
public interface FindSuperUsuarioByKeycloakIdPort {

    /**
     * Finds a SuperUsuario by its Keycloak identity.
     *
     * @param keycloakId the keycloak identity to search for
     * @return an Optional containing the SuperUsuario if found, empty otherwise
     */
    Optional<SuperUsuario> findByKeycloakId(String keycloakId);
}