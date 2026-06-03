package com.ar.crm2.adapter.out.persistence.mapper;

import com.ar.crm2.adapter.out.persistence.entity.AgendaEntity;
import com.ar.crm2.model.entity.Agenda;
import com.ar.crm2.model.vo.AgendaId;
import com.ar.crm2.model.vo.TareaId;
import com.ar.crm2.model.vo.TratoId;
import com.ar.crm2.model.vo.UsuarioId;

import java.util.UUID;

public final class AgendaMapper {

    private AgendaMapper() {}

    public static AgendaEntity toEntity(Agenda domain) {
        return AgendaEntity.builder()
            .id(domain.getId().value().toString())
            .tipo(domain.getTipo())
            .asunto(domain.getAsunto())
            .descripcion(domain.getDescripcion())
            .fecha(domain.getFecha())
            .horaInicio(domain.getHoraInicio())
            .horaFin(domain.getHoraFin())
            .tareaId(domain.getTareaId() != null ? domain.getTareaId().value().toString() : null)
            .tratoId(domain.getTratoId() != null ? domain.getTratoId().value().toString() : null)
            .ubicacion(domain.getUbicacion())
            .linkVideollamada(domain.getLinkVideollamada())
            .creadoPor(domain.getCreadoPor().value().toString())
            .creadoEn(domain.getCreadoEn())
            .actualizadoEn(domain.getActualizadoEn())
            .recordatorioHabilitado(domain.isRecordatorioHabilitado())
            .minutosAntes(domain.getMinutosAntes())
            .recordatorioEstado(domain.getRecordatorioEstado())
            .recordatorioEnviadoEn(domain.getRecordatorioEnviadoEn())
            .ultimoIntentoEn(domain.getUltimoIntentoEn())
            .build();
    }

    public static Agenda toDomain(AgendaEntity entity) {
        if (entity == null) return null;
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Agenda id must not be null");
        }
        if (entity.getTipo() == null) {
            throw new IllegalArgumentException("Agenda tipo must not be null");
        }
        if (entity.getAsunto() == null) {
            throw new IllegalArgumentException("Agenda asunto must not be null");
        }
        if (entity.getFecha() == null) {
            throw new IllegalArgumentException("Agenda fecha must not be null");
        }
        if (entity.getHoraInicio() == null) {
            throw new IllegalArgumentException("Agenda horaInicio must not be null");
        }
        if (entity.getCreadoPor() == null) {
            throw new IllegalArgumentException("Agenda creadoPor must not be null");
        }
        if (entity.getCreadoEn() == null) {
            throw new IllegalArgumentException("Agenda creadoEn must not be null");
        }
        if (entity.getActualizadoEn() == null) {
            throw new IllegalArgumentException("Agenda actualizadoEn must not be null");
        }

        return Agenda.reconstitute(
            AgendaId.from(UUID.fromString(entity.getId())),
            entity.getTipo(),
            entity.getAsunto(),
            entity.getDescripcion(),
            entity.getFecha(),
            entity.getHoraInicio(),
            entity.getHoraFin(),
            entity.getTareaId() != null ? TareaId.from(UUID.fromString(entity.getTareaId())) : null,
            entity.getTratoId() != null ? TratoId.from(UUID.fromString(entity.getTratoId())) : null,
            entity.getUbicacion(),
            entity.getLinkVideollamada(),
            UsuarioId.from(UUID.fromString(entity.getCreadoPor())),
            entity.getCreadoEn(),
            entity.getActualizadoEn(),
            entity.isRecordatorioHabilitado(),
            entity.getMinutosAntes(),
            entity.getRecordatorioEstado(),
            entity.getRecordatorioEnviadoEn(),
            entity.getUltimoIntentoEn()
        );
    }
}
