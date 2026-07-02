package com.ar.crm2.application.ai.port.in;

import com.ar.crm2.application.ai.command.ObtenerConversacionAsistenteCommand;
import com.ar.crm2.model.entity.ia.AiConversacion;
import com.ar.crm2.model.entity.ia.AiMensaje;

import java.util.List;

/**
 * Inbound use case: fetch one AI conversation and its message history.
 */
public interface ObtenerConversacionAsistenteUseCase {

    /**
     * Returns the conversation (always non-null) and the full ordered
     * message history. Throws when the actor does not own the
     * conversation.
     */
    ResultadoConversacionAsistente obtener(ObtenerConversacionAsistenteCommand command);

    record ResultadoConversacionAsistente(AiConversacion conversacion, List<AiMensaje> mensajes) {}
}