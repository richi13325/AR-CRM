package com.ar.crm2.application.identity.model;

/**
 * Request model for provisioning a new user in the identity provider.
 *
 * @param email the user's email (used as username in Keycloak)
 * @param initialPassword the temporary password to set on creation
 * @param enabled whether the user should be enabled upon creation
 */
public record ProvisionIdentityRequest(
    String email,
    String initialPassword,
    boolean enabled
) {}