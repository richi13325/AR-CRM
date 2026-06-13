package com.ar.crm2.application.identity.port.out;

import java.util.Map;

/**
 * Outbound port for setting custom user attributes on an identity in the
 * identity provider.
 * <p>
 * Implemented by infrastructure adapters (e.g., Keycloak Admin REST adapter).
 * <p>
 * Used to push domain-side identifiers (such as {@code usuario_id}) into the
 * identity provider so protocol mappers can emit them as JWT claims.
 */
public interface SetIdentityAttributesPort {

    /**
     * Sets the given custom attributes on the identity in the identity provider.
     * <p>
     * Each entry in {@code attributes} is a single string value. Implementations
     * adapt the payload to the provider's native representation
     * (Keycloak stores user attributes as arrays of strings).
     * The provided map replaces the existing custom-attribute set on the user.
     *
     * @param keycloakId the Keycloak user id
     * @param attributes the attributes to set on the user
     */
    void setAttributes(String keycloakId, Map<String, String> attributes);
}
