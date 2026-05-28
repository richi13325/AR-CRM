package com.ar.crm2.adapter.out.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Keycloak Admin token response (POST /realms/{realm}/protocol/openid-connect/token).
 */
public record KeycloakTokenResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("expires_in") int expiresIn,
    @JsonProperty("refresh_token") String refreshToken
) {}