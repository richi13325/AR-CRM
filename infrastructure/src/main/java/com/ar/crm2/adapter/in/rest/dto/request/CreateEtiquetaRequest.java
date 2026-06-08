package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.TipoEtiqueta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * REST request DTO for creating a new Etiqueta in the global catalog.
 * The name and color are validated at the DTO layer; uniqueness of
 * (nombre, tipoEtiqueta) is enforced at the application boundary.
 */
public record CreateEtiquetaRequest(
    @NotBlank(message = "nombre is required")
    String nombre,

    @NotNull(message = "tipoEtiqueta is required")
    TipoEtiqueta tipoEtiqueta,

    @NotBlank(message = "color is required")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "color must be hex format #RRGGBB")
    String color
) {}
