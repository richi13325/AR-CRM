package com.ar.crm2.application.agenda.command;

import com.ar.crm2.model.enums.TipoAgenda;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateAgendaCommand(
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
    boolean recordatorioHabilitado,
    Integer minutosAntes
) {

    public CreateAgendaCommand {
        if (tipo == null) {
            throw new IllegalArgumentException("tipo is required");
        }
        if (asunto == null || asunto.isBlank()) {
            throw new IllegalArgumentException("asunto is required");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("fecha is required");
        }
        if (horaInicio == null) {
            throw new IllegalArgumentException("horaInicio is required");
        }
        if (creadoPor == null) {
            throw new IllegalArgumentException("creadoPor is required");
        }
        if (recordatorioHabilitado && (minutosAntes == null || minutosAntes <= 0)) {
            throw new IllegalArgumentException("minutosAntes must be greater than 0 when reminder is enabled");
        }
    }
}
