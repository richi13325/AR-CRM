package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.TipoFicha;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * REST request DTO for creating a new Ficha (Kanban card).
 * Required fields validated at construction time.
 * Domain enforces TipoFicha invariants: TAREA requires tareaId and null tratoId,
 * TRATO requires tratoId and null tareaId.
 */
public record CreateFichaRequest(
    @NotNull(message = "columnaId is required")
    UUID columnaId,

    @NotNull(message = "tipoFicha is required")
    TipoFicha tipoFicha,

    UUID tratoId,

    UUID tareaId
) {}