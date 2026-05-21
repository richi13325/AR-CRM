package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.model.entity.Ficha;
import com.ar.crm2.model.enums.TipoFicha;

import java.time.Instant;
import java.util.UUID;

/**
 * REST response DTO for Ficha.
 * Exposes all fields needed for front-end list/create/edit views.
 */
public record FichaResponse(
    UUID id,
    UUID columnaId,
    TipoFicha tipoFicha,
    UUID tratoId,
    UUID tareaId,
    UUID responsableId,
    UUID creadoPor,
    Instant creadoEn,
    Instant actualizadoEn
) {
    /**
     * Maps a domain Ficha to this response DTO.
     */
    public static FichaResponse fromDomain(Ficha ficha) {
        return new FichaResponse(
            ficha.getId().value(),
            ficha.getColumnaId().value(),
            ficha.getTipoFicha(),
            ficha.getTratoId() != null ? ficha.getTratoId().value() : null,
            ficha.getTareaId() != null ? ficha.getTareaId().value() : null,
            ficha.getResponsableId().value(),
            ficha.getCreadoPor().value(),
            ficha.getCreadoEn(),
            ficha.getActualizadoEn()
        );
    }
}