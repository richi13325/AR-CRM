package com.ar.crm2.application.identity.port.out;

import com.ar.crm2.application.identity.model.ProvisionedIdentity;

/**
 * Outbound port for provisioning a new identity in the identity provider.
 * Implemented by infrastructure adapters (e.g., Keycloak Admin REST adapter).
 */
public interface ProvisionIdentityPort {

    /**
     * Provisions a new user in the identity provider.
     *
     * @param email the user's email (used as Keycloak username)
     * @param initialPassword the initial password to set
     * @param enabled whether the user should be enabled
     * @return the provisioned identity with keycloakId
     */
    ProvisionedIdentity provision(String email, String initialPassword, boolean enabled);
}
