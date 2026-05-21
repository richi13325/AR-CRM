package com.ar.crm2.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REST request DTO for creating a new Rol.
 * Required fields validated at construction time.
 */
public record CreateRolRequest(
    @NotBlank(message = "nombre is required")
    @Size(max = 80, message = "nombre must not exceed 80 characters")
    String nombre,

    String descripcion
) {}