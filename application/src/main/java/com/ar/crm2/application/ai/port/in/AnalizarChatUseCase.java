package com.ar.crm2.application.ai.port.in;

import com.ar.crm2.application.ai.command.AnalizarChatCommand;
import com.ar.crm2.application.ai.port.in.result.ResultadoAnalisisChat;

/**
 * Inbound use case: analyze a WhatsApp conversation with the AI
 * assistant.
 *
 * <p>Resolves tenant ownership, loads the WhatsApp transcript and
 * scoped AI summary/memory, calls the AI generation port, persists
 * AI turns and any proposals. Idempotent on subsequent identical
 * requests by the same actor for the same WhatsApp conversation.
 */
public interface AnalizarChatUseCase {

    ResultadoAnalisisChat analizar(AnalizarChatCommand command);
}