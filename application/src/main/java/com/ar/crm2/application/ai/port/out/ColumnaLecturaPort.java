package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.Columna;
import com.ar.crm2.model.enums.TipoTablero;
import com.ar.crm2.model.vo.ColumnaId;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for reading Columna aggregates for the AI use cases.
 *
 * <p>Implemented in infrastructure by the canonical
 * {@code ColumnaRepositoryAdapter}. The catalog shape
 * ({@link #findByTipoTablero}) returns the catalog entries for a
 * given board type — used by the AI tool that needs to enumerate
 * valid target columns before proposing {@code MOVE_KANBAN_FICHA}.
 */
public interface ColumnaLecturaPort {

    Optional<Columna> findById(ColumnaId id);

    /**
     * Returns every catalog column belonging to the given
     * {@link TipoTablero} ({@code TAREAS} or {@code TRATOS}).
     * The AI tool uses this to enumerate the valid target
     * columns for a Kanban move proposal.
     *
     * @param tipoTablero the board type whose catalog columns are
     *                    requested; must not be null
     * @return the columns for the board type, never null (may be empty)
     */
    List<Columna> findByTipoTablero(TipoTablero tipoTablero);
}