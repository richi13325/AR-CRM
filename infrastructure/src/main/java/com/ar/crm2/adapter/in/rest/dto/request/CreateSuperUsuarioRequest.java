package com.ar.crm2.adapter.in.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REST request DTO for creating a new SuperUsuario.
 * Required fields validated at construction time.
 */
public record CreateSuperUsuarioRequest(
    @NotBlank(message = "correo is required")
    @Email(message = "correo must be a valid email")
    @Size(max = 120, message = "correo must not exceed 120 characters")
    String correo,

    @NotBlank(message = "passwordHash is required")
    @Size(max = 255, message = "passwordHash must not exceed 255 characters")
    String passwordHash,

    @Size(max = 255, message = "keycloakId must not exceed 255 characters")
    String keycloakId
) {}