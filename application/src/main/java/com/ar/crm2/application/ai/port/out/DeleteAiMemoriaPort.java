package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.model.entity.ia.AiMemoria;

/**
 * Outbound port for deleting an AI memory record (used to forget or to
 * chain supersede writes atomically at the application boundary).
 */
public interface DeleteAiMemoriaPort {

    void delete(AiMemoria memoria);
}