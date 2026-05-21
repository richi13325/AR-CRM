package com.ar.crm2.application.tablero.command;

import com.ar.crm2.model.enums.TipoTablero;

import java.util.UUID;

/**
 * Command to create a new Tablero.
 * Required fields validated at construction time.
 *
 * <p>The command carries the 4 default columns data so the application layer
 * can assemble the ColumnaTablero list before calling {@link com.ar.crm2.model.entity.Tablero#create}.
 */
public record CreateTableroCommand(
    String nombre,
    String descripcion,
    TipoTablero tipoTablero,
    boolean columnasPredeterminadas,
    UUID superUsuarioId
) {

    public CreateTableroCommand {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("descripcion is required");
        }
        if (tipoTablero == null) {
            throw new IllegalArgumentException("tipoTablero is required");
        }
        if (superUsuarioId == null) {
            throw new IllegalArgumentException("superUsuarioId is required");
        }
    }
}