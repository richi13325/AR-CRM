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
}