package com.ar.crm2.adapter.out.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Keycloak User representation returned after creation.
 */
public record KeycloakUserResponse(
    String id,
    String username,
    @JsonProperty("firstName") String firstName,
    @JsonProperty("lastName") String lastName,
    String email,
    boolean enabled
) {}