package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.NotaTratoEntity;
import com.ar.crm2.model.entity.NotaTrato;
import com.ar.crm2.model.vo.NotaTratoId;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.UsuarioId;

import java.util.UUID;

public final class NotaTratoMapper {

    private NotaTratoMapper() {}

    public static NotaTratoEntity toEntity(NotaTrato d) {
        return NotaTratoEntity.builder()
            .id(d.getId().value().toString())
            .tratoId(d.getTratoId().value().toString())
            .autorId(d.getAutorId() != null ? d.getAutorId().value().toString() : null)
            .tipo(d.getTipo())
            .contenido(d.getContenido())
            .creadoEn(d.getCreadoEn())
            .build();
    }

    public static NotaTrato toDomain(NotaTratoEntity e) {
        return NotaTrato.reconstitute(
            NotaTratoId.from(UUID.fromString(e.getId())),
            TratoId.from(UUID.fromString(e.getTratoId())),
            e.getAutorId() != null ? UsuarioId.from(UUID.fromString(e.getAutorId())) : null,
            e.getTipo(),
            e.getContenido(),
            e.getCreadoEn()
        );
    }
}
