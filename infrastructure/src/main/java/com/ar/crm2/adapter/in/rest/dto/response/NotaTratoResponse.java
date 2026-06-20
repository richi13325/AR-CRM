package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.model.entity.NotaTrato;
import com.ar.crm2.model.enums.TipoNota;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotaTratoResponse(
    UUID id,
    UUID tratoId,
    UUID autorId,
    TipoNota tipo,
    String contenido,
    LocalDateTime creadoEn
) {
    public static NotaTratoResponse fromDomain(NotaTrato n) {
        return new NotaTratoResponse(
            n.getId().value(),
            n.getTratoId().value(),
            n.getAutorId() != null ? n.getAutorId().value() : null,
            n.getTipo(),
            n.getContenido(),
            n.getCreadoEn()
        );
    }
}
