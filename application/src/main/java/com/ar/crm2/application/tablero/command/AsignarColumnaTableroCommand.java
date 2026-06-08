package com.ar.crm2.application.tablero.command;

import java.math.BigDecimal;
import java.util.UUID;

    /**
     * Command to assign an existing catalog Columna to a Tablero with contextual data.
     *
     * <p>Column definition (nombre, color, tipoColumna) belongs to the catalog.
     * This command only carries the board-specific context (WIP limit, note, total).
     */
public record AsignarColumnaTableroCommand(
    UUID tableroId,
    UUID columnaId,
    Integer limiteWip,
    String nota,
    BigDecimal totalValorEstimado
) {

    public AsignarColumnaTableroCommand {
        if (tableroId == null) {
            throw new IllegalArgumentException("tableroId is required");
        }
        if (columnaId == null) {
            throw new IllegalArgumentException("columnaId is required");
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
