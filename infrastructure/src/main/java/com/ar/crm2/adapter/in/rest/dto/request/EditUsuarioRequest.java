package com.ar.crm2.adapter.in.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * REST request DTO for editing an existing Usuario.
 * Authentication is handled by Keycloak — no passwordHash required here.
 * Required fields validated at construction time.
 */
public record EditUsuarioRequest(
    @NotBlank(message = "nombre is required")
    @Size(max = 100, message = "nombre must not exceed 100 characters")
    String nombre,

    @NotBlank(message = "correo is required")
    @Email(message = "correo must be a valid email")
    @Size(max = 120, message = "correo must not exceed 120 characters")
    String correo,

    UUID rolId,

    @Size(max = 255, message = "keycloakId must not exceed 255 characters")
    String keycloakId
) {}