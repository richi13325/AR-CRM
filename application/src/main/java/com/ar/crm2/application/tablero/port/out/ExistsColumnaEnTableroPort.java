package com.ar.crm2.application.tablero.port.out;

import com.ar.crm2.model.vo.ColumnaId;
import com.ar.crm2.model.vo.TableroId;

/**
 * Granular outbound port for checking whether a Columna is already assigned to a Tablero.
 * Used as a duplicate-guard before assigning a catalog column to a board.
 */
public interface ExistsColumnaEnTableroPort {

    /**
     * Checks whether a Columna is already assigned to the given Tablero.
     *
     * @param tableroId the TableroId to check
     * @param columnaId the ColumnaId to check
     * @return true if the column is already assigned to the board, false otherwise
     */
    boolean existsByTableroIdAndColumnaId(TableroId tableroId, ColumnaId columnaId);
}