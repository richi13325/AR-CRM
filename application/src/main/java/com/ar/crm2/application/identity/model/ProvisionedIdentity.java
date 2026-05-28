package com.ar.crm2.application.identity.model;

/**
 * Value object representing the result of a successful Keycloak user provisioning.
 *
 * @param keycloakId the Keycloak user identifier
 * @param email the email used for provisioning
 */
public record ProvisionedIdentity(
    String keycloakId,
    String email
) {}