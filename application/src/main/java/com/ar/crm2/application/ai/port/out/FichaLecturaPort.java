package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.Ficha;
import com.ar.crm2.model.vo.FichaId;

import java.util.Optional;

/**
 * Outbound port for reading Ficha aggregates for the AI use cases.
 */
public interface FichaLecturaPort {

    Optional<Ficha> findById(FichaId id);
}