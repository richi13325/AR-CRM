package com.ar.crm2.application.ficha.port.out;

import com.ar.crm2.model.entity.Ficha;
import com.ar.crm2.model.vo.FichaId;

import java.util.Optional;

/**
 * Granular outbound port for finding a Ficha by its id.
 * Single-method contract per project rules.
 */
public interface FindFichaByIdPort {

    /**
     * Finds a Ficha by its id.
     *
     * @param id the FichaId to search for
     * @return an Optional containing the Ficha if found, empty otherwise
     */
    Optional<Ficha> findById(FichaId id);
}