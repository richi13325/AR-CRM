package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.EstadoRelacion;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * REST request DTO for editing an existing Contacto.
 * Same editable fields as CreateContactoRequest; no id/timestamps/creadoPor.
 * Timestamps (creadoEn, actualizadoEn) are never accepted from the client.
 */
public record EditContactoRequest(
    @NotBlank(message = "nombre is required")
    @Size(min = 1, max = 150, message = "nombre must be 1-150 characters")
    String nombre,

    @Email(message = "correo must be a valid email")
    @Size(max = 150, message = "correo must be at most 150 characters")
    String correo,

    @NotNull(message = "estadoRelacion is required")
    EstadoRelacion estadoRelacion,

    UUID responsableId,

    @Size(max = 50, message = "telefono must be at most 50 characters")
    String telefono,

    @Size(max = 100, message = "cargo must be at most 100 characters")
    String cargo,

    @Size(max = 200, message = "comoNosConocio must be at most 200 characters")
    String comoNosConocio
) {}