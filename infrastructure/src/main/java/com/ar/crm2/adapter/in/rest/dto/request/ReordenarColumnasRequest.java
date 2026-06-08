package com.ar.crm2.adapter.in.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * REST request DTO for reordering columns within a Tablero.
 * The nuevoOrden list must contain exactly the same column UUID values
 * currently present in the Tablero, with no duplicates.
 */
public record ReordenarColumnasRequest(
    @NotNull(message = "nuevoOrden is required")
    @NotEmpty(message = "nuevoOrden cannot be empty")
    @Schema(
        description = "Ordered list of column IDs. Send raw UUID values in the desired final order.",
        example = "[\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"6d9b1a1a-2d69-4b31-9c53-12f98cf5a001\"]"
    )
    List<UUID> nuevoOrden
) {}
