package com.ar.crm2.application.identity.port.out;

/**
 * Outbound port for sending an UPDATE_PASSWORD execute-actions email via the identity provider.
 * Implemented by infrastructure adapters (e.g., Keycloak Admin REST adapter).
 */
public interface SendIdentityUpdatePasswordEmailPort {

    /**
     * Sends an UPDATE_PASSWORD execute-actions email to the user.
     *
     * @param keycloakId the Keycloak user id
     */
    void sendUpdatePasswordEmail(String keycloakId);
}
