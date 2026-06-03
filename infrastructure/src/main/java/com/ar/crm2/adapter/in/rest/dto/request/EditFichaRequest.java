package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.TipoFicha;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * REST request DTO for editing an existing Ficha.
 * Required fields validated at construction time.
 * Domain enforces TipoFicha invariants: TAREA requires tareaId and null tratoId,
 * TRATO requires tratoId and null tareaId.
 */
public record EditFichaRequest(
    @NotNull(message = "columnaId is required")
    UUID columnaId,

    @NotNull(message = "tipo Ficha is required")
    TipoFicha tipoFicha,

    UUID tratoId,

    UUID tareaId
) {}