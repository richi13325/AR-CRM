package com.ar.crm2.application.tarea.port.out;

import com.ar.crm2.model.entity.Tarea;
import com.ar.crm2.model.vo.TareaId;

import java.util.Optional;

/**
 * Granular outbound port for finding a Tarea by its id.
 * Single-method contract per project rules.
 */
public interface FindTareaByIdPort {

    /**
     * Finds a Tarea by its id.
     *
     * @param id the TareaId to search for
     * @return an Optional containing the Tarea if found, empty otherwise
     */
    Optional<Tarea> findById(TareaId id);
}