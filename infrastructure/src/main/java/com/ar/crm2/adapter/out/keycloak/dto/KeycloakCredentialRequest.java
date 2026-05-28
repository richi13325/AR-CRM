package com.ar.crm2.adapter.out.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for setting a user password via Keycloak Credential API.
 */
public record KeycloakCredentialRequest(
    String value,
    String type,
    boolean temporary
) {

    public static KeycloakCredentialRequest initialPassword(String password) {
        return new KeycloakCredentialRequest(password, "password", false);
    }

    /**
     * Factory for the Keycloak Admin REST reset-password endpoint.
     * Payload is identical to initialPassword but named for endpoint clarity.
     */
    public static KeycloakCredentialRequest resetPassword(String password) {
        return new KeycloakCredentialRequest(password, "password", false);
    }
}