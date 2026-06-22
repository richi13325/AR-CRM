package com.ar.crm2.application.columna.command;

import com.ar.crm2.model.enums.TipoColumna;
import com.ar.crm2.model.enums.TipoTablero;

import java.util.Optional;
import java.util.UUID;

/**
 * Command to create a new Columna.
 * Required fields validated at construction time.
 *
 * @param superUsuarioId legacy caller context; authorization is handled by application/security flow, not domain
 * @param nombre         mandatory - column name, 1-80 chars
 * @param color          optional - defaults to #FFFFFF in domain
 * @param tipoTablero    mandatory
 * @param tipoColumna    mandatory
 * @param defaultCatalogBootstrap true only for internal Tablero default catalog bootstrap flow
 */
public record CreateColumnaCommand(
    Optional<UUID> superUsuarioId,
    String nombre,
    String color,
    TipoTablero tipoTablero,
    TipoColumna tipoColumna,
    boolean defaultCatalogBootstrap
) {

    public CreateColumnaCommand(
        Optional<UUID> superUsuarioId,
        String nombre,
        String color,
        TipoTablero tipoTablero,
        TipoColumna tipoColumna
    ) {
        this(superUsuarioId, nombre, color, tipoTablero, tipoColumna, false);
    }

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
