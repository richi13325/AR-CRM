package com.ar.crm2.application.identity.model;

/**
 * Exception thrown when identity provider operations (provision, sync, enable/disable, delete) fail.
 */
public class IdentityProvisioningException extends RuntimeException {

    private final String keycloakId;
    private final Reason reason;

    public enum Reason {
        /** Keycloak server unreachable or connection failed. */
        CONNECTION_FAILURE,
        /** Authentication with Keycloak admin API failed. */
        AUTHENTICATION_FAILURE,
        /** User not found in Keycloak. */
        USER_NOT_FOUND,
        /** Conflict — user already exists. */
        USER_ALREADY_EXISTS,
        /** Generic server error from Keycloak (5xx, etc.). */
        SERVER_ERROR
    }

    public IdentityProvisioningException(String message, Reason reason) {
        super(message);
        this.keycloakId = null;
        this.reason = reason;
    }

    public IdentityProvisioningException(String keycloakId, String message, Reason reason) {
        super(message);
        this.keycloakId = keycloakId;
        this.reason = reason;
    }

    public String getKeycloakId() {
        return keycloakId;
    }

    public Reason getReason() {
        return reason;
    }
}