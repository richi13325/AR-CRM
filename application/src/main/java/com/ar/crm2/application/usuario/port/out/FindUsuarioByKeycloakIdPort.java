package com.ar.crm2.application.usuario.port.out;

import com.ar.crm2.model.entity.Usuario;

import java.util.Optional;

/**
 * Granular outbound port for finding a Usuario by its Keycloak identity.
 * Single-method contract per project rules.
 */
public interface FindUsuarioByKeycloakIdPort {

    /**
     * Finds a Usuario by its Keycloak identity.
     *
     * @param keycloakId the keycloak identity to search for
     * @return an Optional containing the Usuario if found, empty otherwise
     */
    Optional<Usuario> findByKeycloakId(String keycloakId);
}