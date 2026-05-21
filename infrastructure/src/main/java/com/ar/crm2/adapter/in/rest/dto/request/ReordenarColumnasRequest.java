package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.vo.ColumnaId;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * REST request DTO for reordering columns within a Tablero.
 * The nuevoOrden list must contain exactly the same ColumnaId values
 * currently present in the Tablero, with no duplicates.
 */
public record ReordenarColumnasRequest(
    @NotNull(message = "nuevoOrden is required")
    @NotEmpty(message = "nuevoOrden cannot be empty")
    List<ColumnaId> nuevoOrden
) {}