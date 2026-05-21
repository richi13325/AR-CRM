package com.ar.crm2.application.trato.command;

import com.ar.crm2.model.enums.TipoContrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Command to create a new Trato.
 * Required fields validated at construction time.
 */
public record CreateTratoCommand(
    UUID contactoId,
    UUID responsableId,
    String nombre,
    BigDecimal valorEstimado,
    Integer probabilidad,
    LocalDate fechaCierreEsperada,
    TipoContrato tipoContrato
) {

    public CreateTratoCommand {
        if (contactoId == null) {
            throw new IllegalArgumentException("contactoId is required");
        }
        if (responsableId == null) {
            throw new IllegalArgumentException("responsableId is required");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
    }
}