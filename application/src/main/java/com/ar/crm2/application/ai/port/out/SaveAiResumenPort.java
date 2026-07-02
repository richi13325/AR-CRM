package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.ia.AiResumenContexto;

/**
 * Outbound port for persisting an AI context summary.
 */
public interface SaveAiResumenPort {

    AiResumenContexto save(AiResumenContexto resumen);
}