package com.ar.crm2.adapter.in.rest.dto.request;

import jakarta.validation.constraints.Size;

/**
 * REST request DTO for editing an existing Rol.
 * Does NOT include activo — that is preserved at the domain layer.
 */
public record EditRolRequest(
    @Size(max = 80, message = "nombre must not exceed 80 characters")
    String nombre,

    String descripcion
) {}