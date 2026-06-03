package com.ar.crm2.application.tablero.port.out;

import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.enums.TipoTablero;

import java.util.Optional;

/**
 * Output port for finding the initial/default column of a board by its type.
 * The initial column is the first column in the board's column list.
 */
public interface FindInitialColumnPort {

    /**
     * Finds the initial column for a board of the given type.
     *
     * @param tipoTablero the type of board (TAREAS or TRATOS)
     * @return optional containing the initial column, if a board of that type exists
     */
    Optional<Columna> findInitialColumn(TipoTablero tipoTablero);
}
