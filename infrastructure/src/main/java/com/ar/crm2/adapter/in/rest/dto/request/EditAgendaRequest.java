package com.ar.crm2.adapter.in.rest.dto.request;

import com.ar.crm2.model.enums.TipoAgenda;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record EditAgendaRequest(
    @NotNull(message = "tipo is required")
    TipoAgenda tipo,

    @NotBlank(message = "asunto is required")
    @Size(min = 1, max = 200, message = "asunto must be 1-200 characters")
    String asunto,

    String descripcion,

    @NotNull(message = "fecha is required")
    LocalDate fecha,

    @NotNull(message = "horaInicio is required")
    LocalTime horaInicio,

    LocalTime horaFin,

    UUID tareaId,

    UUID tratoId,

    String ubicacion,

    String linkVideollamada,

    Boolean recordatorioHabilitado,

    @Min(value = 1, message = "minutosAntes must be at least 1")
    Integer minutosAntes
) {}
