package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.EstadoRelacion;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * REST request DTO for creating a new Contacto.
 * Only empresaId and nombre are required.
 * Timestamps (creadoEn, actualizadoEn) and id are never accepted from the client.
 */
public record CreateContactoRequest(
    @NotNull(message = "empresaId is required")
    UUID empresaId,

    @NotBlank(message = "nombre is required")
    @Size(min = 1, max = 150, message = "nombre must be 1-150 characters")
    String nombre,

    @Email(message = "correo must be a valid email")
    @Size(max = 150, message = "correo must be at most 150 characters")
    String correo,

    @NotNull(message = "estadoRelacion is required")
    EstadoRelacion estadoRelacion,

    UUID responsableId,
    UUID creadoPor,

    @Size(max = 50, message = "telefono must be at most 50 characters")
    String telefono,

    @Size(max = 100, message = "cargo must be at most 100 characters")
    String cargo,

    @Size(max = 200, message = "comoNosConocio must be at most 200 characters")
    String comoNosConocio
) {}