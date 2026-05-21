package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.EstadoRelacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * REST request DTO for editing an existing Empresa.
 * Same editable fields as CreateEmpresaRequest; no id/timestamps/creadoPor.
 * Timestamps (creadoEn, actualizadoEn) are never accepted from the client.
 */
public record EditEmpresaRequest(
    @NotBlank(message = "nombre is required")
    @Size(min = 1, max = 200, message = "nombre must be 1-200 characters")
    String nombre,

    String sector,
    String telefono,
    String paginaWeb,
    String facebook,
    String instagram,
    String twitter,
    EstadoRelacion estadoRelacion,
    UUID responsableId,
    String notas
) {}