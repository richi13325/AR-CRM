package com.ar.crm2.application.tablero.command;

import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoEstadoColumnaTableroTarea;
import com.ar.crm2.model.enums.TipoEstadoColumnaTableroTrato;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to add a column to an existing Tablero.
 *
 * <p>The command carries both the tableroId and the column definition.
 * The application layer builds the {@link com.ar.crm2.model.entity.ColumnaTablero}
 * contextual wrapper and calls {@link com.ar.crm2.model.entity.Tablero#agregarColumnaTablero}.
 */
public record AgregarColumnaTableroCommand(
    UUID tableroId,
    String nombre,
    String color,
    TipoColumna tipoColumna,
    Integer limiteWip,
    String nota,
    TipoEstadoColumnaTableroTarea estadoTarea,
    TipoEstadoColumnaTableroTrato estadoTrato,
    BigDecimal totalValorEstimado,
    boolean existeOtraColumnaConMismoNombre
) {

    public AgregarColumnaTableroCommand {
        if (tableroId == null) {
            throw new IllegalArgumentException("tableroId is required");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
        if (tipoColumna == null) {
            throw new IllegalArgumentException("tipoColumna is required");
        }
        if (limiteWip == null) {
            throw new IllegalArgumentException("limiteWip is required");
        }
        if (limiteWip <= 0) {
            throw new IllegalArgumentException("limiteWip must be greater than zero");
        }
        if (totalValorEstimado == null) {
            throw new IllegalArgumentException("totalValorEstimado is required");
        }
    }
}