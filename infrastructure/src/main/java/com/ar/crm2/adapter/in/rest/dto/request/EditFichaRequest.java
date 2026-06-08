package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.TipoFicha;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * REST request DTO for editing an existing Ficha.
 * Required fields validated at construction time.
 * Domain enforces TipoFicha invariants: TAREA requires tareaId and null tratoId,
 * TRATO requires tratoId and null tareaId.
 *
 * <p>The {@code etiquetaIds} list, when present (non-null), replaces the
 * existing etiqueta relations in full. Pass an empty list to clear all
 * tags; pass null to leave the existing tags untouched.
 */
public record EditFichaRequest(
    @NotNull(message = "columnaId is required")
    UUID columnaId,

    @NotNull(message = "tipo Ficha is required")
    TipoFicha tipoFicha,

    UUID tratoId,

    UUID tareaId,

    List<UUID> etiquetaIds
) {}