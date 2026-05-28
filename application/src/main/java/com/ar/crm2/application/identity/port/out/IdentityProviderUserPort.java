package com.ar.crm2.application.identity.port.out;

import com.ar.crm2.application.identity.model.IdentityProvisioningException;
import com.ar.crm2.application.identity.model.ProvisionedIdentity;

/**
 * Outbound port for identity provider operations.
 * Implemented by infrastructure adapters (e.g., Keycloak Admin REST adapter).
 */
public interface IdentityProviderUserPort {

    /**
     * Provisions a new user in the identity provider.
     *
     * @param email the user's email (used as Keycloak username)
     * @param initialPassword the initial password to set
     * @param enabled whether the user should be enabled
     * @return the provisioned identity with keycloakId
     * @throws IdentityProvisioningException if provisioning fails
     */
    ProvisionedIdentity provision(String email, String initialPassword, boolean enabled);

    /**
     * Synchronizes an email change in the identity provider.
     *
     * @param keycloakId the Keycloak user id
     * @param email the new email
     * @throws IdentityProvisioningException if sync fails
     */
    void syncEmail(String keycloakId, String email);

    /**
     * Enables or disables a user in the identity provider.
     *
     * @param keycloakId the Keycloak user id
     * @param enabled true to enable, false to disable
     * @throws IdentityProvisioningException if operation fails
     */
    void setEnabled(String keycloakId, boolean enabled);

    /**
     * Deletes a user from the identity provider.
     *
     * @param keycloakId the Keycloak user id
     * @throws IdentityProvisioningException if deletion fails
     */
    void delete(String keycloakId);
}