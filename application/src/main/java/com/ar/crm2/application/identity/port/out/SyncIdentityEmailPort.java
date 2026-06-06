package com.ar.crm2.application.identity.port.out;

/**
 * Outbound port for synchronizing an email change to the identity provider.
 * Implemented by infrastructure adapters (e.g., Keycloak Admin REST adapter).
 */
public interface SyncIdentityEmailPort {

    /**
     * Synchronizes an email change in the identity provider.
     *
     * @param keycloakId the Keycloak user id
     * @param email the new email
     */
    void syncEmail(String keycloakId, String email);
}
