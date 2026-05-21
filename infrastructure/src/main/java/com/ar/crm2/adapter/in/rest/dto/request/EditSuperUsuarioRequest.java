package com.ar.crm2.adapter.in.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REST request DTO for editing an existing SuperUsuario.
 * Required fields validated at construction time.
 * passwordHash intentionally excluded — edit does not modify password.
 */
public record EditSuperUsuarioRequest(
    @NotBlank(message = "correo is required")
    @Email(message = "correo must be a valid email")
    @Size(max = 120, message = "correo must not exceed 120 characters")
    String correo
) {}