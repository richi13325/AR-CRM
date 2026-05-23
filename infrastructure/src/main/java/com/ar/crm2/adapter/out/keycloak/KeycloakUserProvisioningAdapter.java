package com.ar.crm2.adapter.out.keycloak;

import com.ar.crm2.adapter.out.keycloak.dto.KeycloakCredentialRequest;
import com.ar.crm2.adapter.out.keycloak.dto.KeycloakProvisionRequest;
import com.ar.crm2.adapter.out.keycloak.dto.KeycloakTokenResponse;
import com.ar.crm2.adapter.out.keycloak.dto.KeycloakUserResponse;
import com.ar.crm2.application.identity.model.IdentityProvisioningException;
import com.ar.crm2.application.identity.model.ProvisionedIdentity;
import com.ar.crm2.config.KeycloakAdminProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.util.List;
import java.util.Map;

import reactor.core.publisher.Mono;

/**
 * Infrastructure adapter implementing {@link com.ar.crm2.application.identity.port.out.IdentityProviderUserPort}
 * using the Keycloak Admin REST API.
 *
 * <p>Authentication uses the confidential client grant (client_id + client_secret)
 * to obtain a bearer token before each admin operation.
 *
 * <p>All operations translate Keycloak errors into {@link IdentityProvisioningException}
 * with appropriate {@link IdentityProvisioningException.Reason}.
 */
@Component
public class KeycloakUserProvisioningAdapter implements com.ar.crm2.application.identity.port.out.IdentityProviderUserPort {

    private final KeycloakAdminProperties props;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public KeycloakUserProvisioningAdapter(
            KeycloakAdminProperties props,
            ObjectMapper objectMapper
    ) {
        this.props = props;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(props.getServerUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public ProvisionedIdentity provision(String email, String initialPassword, boolean enabled) {
        String token = obtainAdminToken();
        String username = email; // Keycloak uses email as username

        // Create user with VERIFY_EMAIL required action
        KeycloakProvisionRequest provisionRequest = KeycloakProvisionRequest.createNew(
            username,
            email,
            enabled
        );

        // Keycloak returns 201 Created with Location header: /admin/realms/{realm}/users/{id}
        // Use exchangeToMono to capture the Location header in a single call.
        String location = webClient.post()
                .uri("/admin/realms/{realm}/users", props.getRealm())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(provisionRequest)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        List<String> locationHeaders = response.headers().header(HttpHeaders.LOCATION);
                        if (locationHeaders != null && !locationHeaders.isEmpty()) {
                            return Mono.just(locationHeaders.get(0));
                        }
                        return Mono.just("");
                    } else {
                        return response.createException()
                                .map(ex -> {
                                    throw handleKeycloakError(ex, null);
                                });
                    }
                })
                .block();

        // Extract userId from Location header: {server}/admin/realms/{realm}/users/{id}
        String keycloakId = extractUserIdFromLocation(location);

        // Set the initial password
        setInitialPassword(keycloakId, initialPassword, token);

        return new ProvisionedIdentity(keycloakId, email);
    }

    private void setInitialPassword(String keycloakId, String password, String token) {
        KeycloakCredentialRequest credRequest = KeycloakCredentialRequest.initialPassword(password);
        webClient.put()
                .uri("/admin/realms/{realm}/users/{id}/credentials", props.getRealm(), keycloakId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(credRequest)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Override
    public void syncEmail(String keycloakId, String email) {
        String token = obtainAdminToken();
        Map<String, Object> updatePayload = Map.of("email", email, "username", email);
        webClient.put()
                .uri("/admin/realms/{realm}/users/{id}", props.getRealm(), keycloakId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(updatePayload)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Override
    public void setEnabled(String keycloakId, boolean enabled) {
        String token = obtainAdminToken();
        Map<String, Object> updatePayload = Map.of("enabled", enabled);
        webClient.put()
                .uri("/admin/realms/{realm}/users/{id}", props.getRealm(), keycloakId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(updatePayload)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Override
    public void delete(String keycloakId) {
        String token = obtainAdminToken();
        webClient.delete()
                .uri("/admin/realms/{realm}/users/{id}", props.getRealm(), keycloakId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    /**
     * Obtains an admin access token using client credentials grant.
     */
    private String obtainAdminToken() {
        Map<String, String> tokenRequest = Map.of(
            "grant_type", "client_credentials",
            "client_id", props.getClientId(),
            "client_secret", props.getClientSecret()
        );

        KeycloakTokenResponse tokenResponse = webClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", props.getRealm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(tokenRequest)
                .retrieve()
                .bodyToMono(KeycloakTokenResponse.class)
                .block();

        return tokenResponse.accessToken();
    }

    private String extractUserIdFromLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IdentityProvisioningException(
                "Keycloak did not return a Location header after user creation",
                IdentityProvisioningException.Reason.SERVER_ERROR
            );
        }
        // Location format: {server}/admin/realms/{realm}/users/{id}
        String[] parts = location.split("/");
        return parts[parts.length - 1];
    }

    private RuntimeException handleKeycloakError(WebClientResponseException ex, String keycloakId) {
        int status = ex.getStatusCode() != null ? ex.getStatusCode().value() : 500;
        String body = ex.getResponseBodyAsString();

        if (status == 404) {
            return new IdentityProvisioningException(
                keycloakId,
                "User not found in Keycloak: " + body,
                IdentityProvisioningException.Reason.USER_NOT_FOUND
            );
        }
        if (status == 409) {
            return new IdentityProvisioningException(
                keycloakId,
                "User already exists in Keycloak: " + body,
                IdentityProvisioningException.Reason.USER_ALREADY_EXISTS
            );
        }
        if (status == 401) {
            return new IdentityProvisioningException(
                "Keycloak admin authentication failed: " + body,
                IdentityProvisioningException.Reason.AUTHENTICATION_FAILURE
            );
        }
        if (status >= 500) {
            return new IdentityProvisioningException(
                keycloakId,
                "Keycloak server error: " + body,
                IdentityProvisioningException.Reason.SERVER_ERROR
            );
        }
        return new IdentityProvisioningException(
            keycloakId,
            "Keycloak request failed (" + status + "): " + body,
            IdentityProvisioningException.Reason.SERVER_ERROR
        );
    }
}