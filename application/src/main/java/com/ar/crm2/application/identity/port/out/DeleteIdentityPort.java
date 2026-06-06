package com.ar.crm2.application.identity.port.out;

/**
 * Outbound port for deleting an identity from the identity provider.
 * Implemented by infrastructure adapters (e.g., Keycloak Admin REST adapter).
 */
public interface DeleteIdentityPort {

    /**
     * Deletes a user from the identity provider.
     *
     * @param keycloakId the Keycloak user id
     */
    void delete(String keycloakId);
}
