package com.ar.crm2.adapter.out.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for Keycloak User creation request (POST /admin/realms/{realm}/users).
 */
public record KeycloakProvisionRequest(
    String username,
    Email email,
    boolean enabled,
    List<String> requiredActions
) {

    /**
     * Nested email representation.
     */
    public record Email(String value) {
    }

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
            new Email(email),
            enabled,
            List.of("VERIFY_EMAIL")
        );
    }
}