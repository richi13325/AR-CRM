package com.ar.crm2.adapter.in.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REST request DTO for creating a new SuperUsuario.
 * Authentication is handled by Keycloak — no passwordHash required here.
 * Required fields validated at construction time.
 */
public record CreateSuperUsuarioRequest(
    @NotBlank(message = "correo is required")
    @Email(message = "correo must be a valid email")
    @Size(max = 120, message = "correo must not exceed 120 characters")
    String correo,

    @NotBlank(message = "initialPassword is required for Keycloak provisioning")
    String initialPassword,

    @Size(max = 255, message = "keycloakId must not exceed 255 characters")
    String keycloakId
) {}