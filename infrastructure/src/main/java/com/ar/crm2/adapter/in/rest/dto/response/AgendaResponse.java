package com.ar.crm2.adapter.in.rest.dto.response;

import com.ar.crm2.model.entity.Agenda;
import com.ar.crm2.model.enums.RecordatorioEstado;
import com.ar.crm2.model.enums.TipoAgenda;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record AgendaResponse(
    UUID id,
    TipoAgenda tipo,
    String asunto,
    String descripcion,
    LocalDate fecha,
    LocalTime horaInicio,
    LocalTime horaFin,
    UUID tareaId,
    UUID tratoId,
    String ubicacion,
    String linkVideollamada,
    UUID creadoPor,
    LocalDateTime creadoEn,
    LocalDateTime actualizadoEn,
    boolean recordatorioHabilitado,
    Integer minutosAntes,
    RecordatorioEstado recordatorioEstado,
    LocalDateTime recordatorioEnviadoEn,
    LocalDateTime ultimoIntentoEn
) {
    public static AgendaResponse fromDomain(Agenda agenda) {
        return new AgendaResponse(
            agenda.getId().value(),
            agenda.getTipo(),
            agenda.getAsunto(),
            agenda.getDescripcion(),
            agenda.getFecha(),
            agenda.getHoraInicio(),
            agenda.getHoraFin(),
            agenda.getTareaId() != null ? agenda.getTareaId().value() : null,
            agenda.getTratoId() != null ? agenda.getTratoId().value() : null,
            agenda.getUbicacion(),
            agenda.getLinkVideollamada(),
            agenda.getCreadoPor().value(),
            agenda.getCreadoEn(),
            agenda.getActualizadoEn(),
            agenda.isRecordatorioHabilitado(),
            agenda.getMinutosAntes(),
            agenda.getRecordatorioEstado(),
            agenda.getRecordatorioEnviadoEn(),
            agenda.getUltimoIntentoEn()
        );
    }
}
