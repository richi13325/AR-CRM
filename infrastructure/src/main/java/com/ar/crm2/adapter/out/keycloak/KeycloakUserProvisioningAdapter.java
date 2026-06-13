package com.ar.crm2.adapter.out.keycloak;

import com.ar.crm2.adapter.out.keycloak.dto.KeycloakCredentialRequest;
import com.ar.crm2.adapter.out.keycloak.dto.KeycloakProvisionRequest;
import com.ar.crm2.adapter.out.keycloak.dto.KeycloakTokenResponse;
import com.ar.crm2.application.identity.model.IdentityProvisioningException;
import com.ar.crm2.application.identity.model.ProvisionedIdentity;
import com.ar.crm2.config.KeycloakAdminProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import reactor.core.publisher.Mono;

public class KeycloakUserProvisioningAdapter
        implements
            com.ar.crm2.application.identity.port.out.ProvisionIdentityPort,
            com.ar.crm2.application.identity.port.out.SyncIdentityEmailPort,
            com.ar.crm2.application.identity.port.out.SetIdentityEnabledPort,
            com.ar.crm2.application.identity.port.out.SetIdentityAttributesPort,
            com.ar.crm2.application.identity.port.out.DeleteIdentityPort,
            com.ar.crm2.application.identity.port.out.SendIdentityUpdatePasswordEmailPort {

    private final KeycloakAdminProperties props;
    private final WebClient webClient;

    public KeycloakUserProvisioningAdapter(
            KeycloakAdminProperties props
    ) {
        this.props = props;
        this.webClient = WebClient.builder()
                .baseUrl(props.getServerUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public ProvisionedIdentity provision(String email, String initialPassword, boolean enabled) {
        String token = obtainAdminToken();
        String username = email; // Keycloak uses email as username

        KeycloakProvisionRequest provisionRequest = KeycloakProvisionRequest.createNew(
            username,
            email,
            enabled
        );

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

        String keycloakId = extractUserIdFromLocation(location);

        try {
            KeycloakCredentialRequest credRequest = KeycloakCredentialRequest.resetPassword(initialPassword);
            webClient.put()
                    .uri("/admin/realms/{realm}/users/{id}/reset-password", props.getRealm(), keycloakId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .bodyValue(credRequest)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (RuntimeException ePassword) {
            try {
                webClient.delete()
                        .uri("/admin/realms/{realm}/users/{id}", props.getRealm(), keycloakId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
            } catch (RuntimeException eDelete) {
                throw new IdentityProvisioningException(
                    keycloakId,
                    "Failed to set password on provisioned Keycloak user, and compensation delete also failed: "
                        + eDelete.getMessage(),
                    IdentityProvisioningException.Reason.SERVER_ERROR
                );
            }
            throw new IdentityProvisioningException(
                keycloakId,
                "Failed to set initial password on provisioned Keycloak user: " + ePassword.getMessage(),
                IdentityProvisioningException.Reason.SERVER_ERROR
            );
        }

        try {
            sendVerificationEmail(keycloakId, token);
        } catch (RuntimeException eEmail) {
            try {
                webClient.delete()
                        .uri("/admin/realms/{realm}/users/{id}", props.getRealm(), keycloakId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
            } catch (RuntimeException eDelete) {
                throw new IdentityProvisioningException(
                    keycloakId,
                    "Failed to send verification email on provisioned Keycloak user, and compensation delete also failed: "
                        + eDelete.getMessage(),
                    IdentityProvisioningException.Reason.SERVER_ERROR
                );
            }
            throw new IdentityProvisioningException(
                keycloakId,
                "Failed to send verification email on provisioned Keycloak user: " + eEmail.getMessage(),
                IdentityProvisioningException.Reason.SERVER_ERROR
            );
        }

        return new ProvisionedIdentity(keycloakId, email);
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
    public void setAttributes(String keycloakId, Map<String, String> attributes) {
        String token = obtainAdminToken();
        Map<String, Object> currentRepresentation = fetchUserRepresentation(keycloakId, token);
        // Keycloak stores user attributes as Map<String, List<String>>.
        Map<String, List<String>> keycloakAttributes = attributes.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> List.of(e.getValue())
                ));

        Map<String, Object> mergedAttributes = new LinkedHashMap<>();
        Object existingAttributes = currentRepresentation.get("attributes");
        if (existingAttributes instanceof Map<?, ?> existingAttributeMap) {
            existingAttributeMap.forEach((key, value) -> {
                if (key instanceof String attributeName) {
                    mergedAttributes.put(attributeName, value);
                }
            });
        }
        mergedAttributes.putAll(keycloakAttributes);

        Map<String, Object> updatePayload = new LinkedHashMap<>(currentRepresentation);
        updatePayload.put("attributes", mergedAttributes);
        webClient.put()
                .uri("/admin/realms/{realm}/users/{id}", props.getRealm(), keycloakId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(updatePayload)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private Map<String, Object> fetchUserRepresentation(String keycloakId, String token) {
        Map<String, Object> userRepresentation = webClient.get()
                .uri("/admin/realms/{realm}/users/{id}", props.getRealm(), keycloakId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        if (userRepresentation == null || userRepresentation.isEmpty()) {
            throw new IdentityProvisioningException(
                keycloakId,
                "Keycloak returned an empty user representation during attribute sync",
                IdentityProvisioningException.Reason.SERVER_ERROR
            );
        }

        return userRepresentation;
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

    @Override
    public void sendUpdatePasswordEmail(String keycloakId) {
        String token = obtainAdminToken();
        List<String> actions = List.of("UPDATE_PASSWORD");
        webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/realms/{realm}/users/{id}/execute-actions-email")
                        .queryParam("client_id", "account")
                        .build(props.getRealm(), keycloakId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(actions)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private void sendVerificationEmail(String keycloakId, String token) {
        webClient.put()
                .uri("/admin/realms/{realm}/users/{id}/send-verify-email?client_id=account", props.getRealm(), keycloakId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private String obtainAdminToken() {
        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add("grant_type", "client_credentials");
        tokenRequest.add("client_id", props.getClientId());
        tokenRequest.add("client_secret", props.getClientSecret());

        KeycloakTokenResponse tokenResponse = webClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", props.getRealm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(tokenRequest))
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
