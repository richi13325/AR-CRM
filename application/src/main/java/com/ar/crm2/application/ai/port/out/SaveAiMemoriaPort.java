package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.ia.AiMemoria;

/**
 * Outbound port for persisting an AI memory record.
 */
public interface SaveAiMemoriaPort {

    AiMemoria save(AiMemoria memoria);
}