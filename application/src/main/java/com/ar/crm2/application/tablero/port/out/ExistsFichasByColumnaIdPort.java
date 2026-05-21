package com.ar.crm2.application.tablero.port.out;

import com.ar.crm2.model.vo.ColumnaId;

/**
 * Granular outbound port for checking whether a Columna contains any Fichas.
 * Used as a guard before removing a column from a Tablero.
 *
 * <p>NOTE: This port requires {@link com.ar.crm2.model.entity.FichaEntity} to exist
 * in the infrastructure layer. If it does not exist yet, use the stub adapter
 * that returns {@code false} until the real implementation is available.
 */
public interface ExistsFichasByColumnaIdPort {

    /**
     * Checks whether any Fichas are associated with the given Columna.
     *
     * @param columnaId the ColumnaId to check
     * @return true if at least one Ficha exists in the column, false otherwise
     */
    boolean existsFichasByColumnaId(ColumnaId columnaId);
}