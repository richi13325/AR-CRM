package com.ar.crm2.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * REST request DTO for editing an existing Etiqueta.
 * The id comes in as a request parameter; only the editable fields
 * (nombre, color) live in the body. Tipo is immutable: it is a structural
 * property of the catalog row, not a field that drifts over time.
 */
public record EditEtiquetaRequest(
    @NotBlank(message = "nombre is required")
    String nombre,

    @NotBlank(message = "color is required")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "color must be hex format #RRGGBB")
    String color
) {}
