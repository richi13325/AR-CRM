package com.ar.crm2.adapter.out.keycloak.dto;

import java.util.List;

/**
 * DTO for Keycloak User creation request (POST /admin/realms/{realm}/users).
 */
public record KeycloakProvisionRequest(
    String username,
    String email,
    boolean enabled,
    List<String> requiredActions
) {

    /**
     * Factory method to build a new user with VERIFY_EMAIL required action.
     */
    public static KeycloakProvisionRequest createNew(
        String username,
        String email,
        boolean enabled
    ) {
        return new KeycloakProvisionRequest(
            username,
            email,
            enabled,
            List.of("VERIFY_EMAIL")
        );
    }
}