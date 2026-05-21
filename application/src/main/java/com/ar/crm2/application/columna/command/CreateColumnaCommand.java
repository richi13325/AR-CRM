package com.ar.crm2.application.columna.command;

import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;

import java.util.Optional;
import java.util.UUID;

/**
 * Command to create a new Columna.
 * Required fields validated at construction time.
 *
 * @param superUsuarioId optional for PERSONALIZADA, mandatory for PREDETERMINADA (enforced by domain)
 * @param nombre         mandatory - column name, 1-80 chars
 * @param color          optional - defaults to #FFFFFF in domain
 * @param tipoTablero    mandatory
 * @param tipoColumna    mandatory
 */
public record CreateColumnaCommand(
    Optional<UUID> superUsuarioId,
    String nombre,
    String color,
    TipoTablero tipoTablero,
    TipoColumna tipoColumna
) {

    public CreateColumnaCommand {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre is required");
        }
        if (tipoTablero == null) {
            throw new IllegalArgumentException("tipoTablero is required");
        }
        if (tipoColumna == null) {
            throw new IllegalArgumentException("tipoColumna is required");
        }
    }
}