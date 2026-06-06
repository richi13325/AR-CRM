package com.ar.crm2.application.identity.port.out;

/**
 * Outbound port for enabling or disabling an identity in the identity provider.
 * Implemented by infrastructure adapters (e.g., Keycloak Admin REST adapter).
 */
public interface SetIdentityEnabledPort {

    /**
     * Enables or disables a user in the identity provider.
     *
     * @param keycloakId the Keycloak user id
     * @param enabled true to enable, false to disable
     */
    void setEnabled(String keycloakId, boolean enabled);
}
