package com.ar.crm2.application.ai.port.in;

import com.ar.crm2.application.ai.command.ListarConversacionesAsistenteCommand;
import com.ar.crm2.model.entity.ia.AiConversacion;

import java.util.List;

/**
 * Inbound use case: list AI conversations for the requester scope.
 */
public interface ListarConversacionesAsistenteUseCase {

    List<AiConversacion> listar(ListarConversacionesAsistenteCommand command);
}