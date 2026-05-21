package com.ar.crm2.application.tablero.port.in;

import com.ar.crm2.application.tablero.command.AgregarColumnaTableroCommand;
import com.ar.crm2.model.entity.Tablero;

/**
 * Inbound input port for adding a column to an existing Tablero.
 * UseCase suffix per project rules.
 */
public interface AgregarColumnaTableroUseCase {

    /**
     * Adds a new column to an existing Tablero.
     *
     * @param command holds the tableroId and column definition
     * @return the updated Tablero aggregate
     */
    Tablero agregarColumna(AgregarColumnaTableroCommand command);
}