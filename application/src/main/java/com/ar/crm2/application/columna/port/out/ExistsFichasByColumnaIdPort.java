package com.ar.crm2.application.columna.port.out;

import com.ar.crm2.model.vo.ColumnaId;

/**
 * Granular outbound port for checking if a Columna has associated Fichas.
 * Single-method contract per project rules.
 * Used as delete guard to prevent removal of Columnas assigned to Fichas.
 * Own port under columna namespace; NOT reused from tablero vertical.
 */
public interface ExistsFichasByColumnaIdPort {

    /**
     * Checks whether any Fichas are associated with the given Columna.
     *
     * @param id the ColumnaId to check
     * @return true if Fichas exist for this Columna, false otherwise
     */
    boolean existsFichasByColumnaId(ColumnaId id);
}