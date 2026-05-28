package com.ar.crm2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Keycloak Admin API access.
 * Bound from application.yml under the "crm2.keycloak.admin" prefix.
 *
 * <p>Located in infrastructure so the Keycloak adapter (an outbound infra adapter)
 * can inject these properties without creating a cross-module dependency on boot.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "crm2.keycloak.admin")
public class KeycloakAdminProperties {

    /**
     * Keycloak server base URL (e.g., http://localhost:8180).
     */
    private String serverUrl;

    /**
     * Target realm name (e.g., crm2-local).
     */
    private String realm;

    /**
     * Client id for the admin service account.
     */
    private String clientId;

    /**
     * Client secret for the admin service account.
     */
    private String clientSecret;

    /**
     * Connection timeout in milliseconds.
     */
    private int connectTimeoutMs = 5000;

    /**
     * Read timeout in milliseconds.
     */
    private int readTimeoutMs = 10000;
}