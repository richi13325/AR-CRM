package com.ar.crm2.application.ai.port.in;

import com.ar.crm2.application.ai.command.RegistrarMensajeAsistenteCommand;
import com.ar.crm2.application.ai.port.in.result.ResultadoAnalisisChat;

/**
 * Inbound use case: register a follow-up user message in an existing
 * AI conversation and obtain the assistant's next reply.
 */
public interface RegistrarMensajeAsistenteUseCase {

    ResultadoAnalisisChat registrar(RegistrarMensajeAsistenteCommand command);
}