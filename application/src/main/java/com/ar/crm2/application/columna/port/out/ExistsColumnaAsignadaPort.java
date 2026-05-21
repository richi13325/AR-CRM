package com.ar.crm2.application.columna.port.out;

import com.ar.crm2.model.vo.ColumnaId;

/**
 * Granular outbound port for checking whether a Columna is assigned to any Tablero.
 * Used as a delete guard before hard-deleting a catalog column.
 *
 * <p>Note: This is distinct from {@link ExistsFichasByColumnaIdPort} which guards
 * against deleting columns that have fichas. This port guards against deleting
 * catalog columns that are still assigned to boards.
 */
public interface ExistsColumnaAsignadaPort {

    /**
     * Checks whether a Columna is assigned to at least one Tablero.
     *
     * @param columnaId the ColumnaId to check
     * @return true if the column is assigned to any board, false otherwise
     */
    boolean existsByColumnaId(ColumnaId columnaId);
}