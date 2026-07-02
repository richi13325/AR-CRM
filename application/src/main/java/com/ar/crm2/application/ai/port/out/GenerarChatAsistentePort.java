package com.ar.crm2.application.ai.port.out;

import com.ar.crm2.application.ai.port.out.dto.RespuestaAsistente;
import com.ar.crm2.application.ai.port.out.dto.ChatAsistenteRequest;

/**
 * Outbound port for invoking the AI assistant model. Implemented in
 * infrastructure by the Spring AI / OpenAI adapter (PR 3).
 *
 * <p>The request and result types are application-owned — no Spring
 * AI types cross this port boundary.
 */
public interface GenerarChatAsistentePort {

    /**
     * Runs the assistant for the supplied request.
     *
     * @param solicitud the prepared assistant request
     * @return the assistant reply and any staged action proposals
     */
    RespuestaAsistente generar(ChatAsistenteRequest solicitud);
}