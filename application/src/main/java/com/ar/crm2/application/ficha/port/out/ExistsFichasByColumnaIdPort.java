package com.ar.crm2.application.ficha.port.out;

import com.ar.crm2.model.vo.ColumnaId;

/**
 * Granular outbound port for checking whether a Columna has any associated Fichas.
 * Single-method contract per project rules.
 *
 * <p>Owned by the Ficha vertical because the query operates over Ficha persistence.
 * Used as a delete guard by both the Tablero flow
 * (preventing removal of a column that still has fichas) and the Columna flow
 * (preventing hard-deletion of a catalog column that still has fichas).
 */
public interface ExistsFichasByColumnaIdPort {

    /**
     * Checks whether any Fichas are associated with the given Columna.
     *
     * @param id the ColumnaId to check
     * @return true if at least one Ficha exists for the column, false otherwise
     */
    boolean existsFichasByColumnaId(ColumnaId id);
}
