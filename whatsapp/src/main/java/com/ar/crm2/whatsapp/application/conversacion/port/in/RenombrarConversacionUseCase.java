package com.ar.crm2.whatsapp.application.conversacion.port.in;

import com.ar.crm2.whatsapp.domain.entity.Conversacion;

import java.util.UUID;

/**
 * Renombra manualmente el contacto de una conversación desde el chat. Actualiza
 * tanto el nombre mostrado en la conversación como el Contacto del CRM vinculado.
 */
public interface RenombrarConversacionUseCase {
    Conversacion renombrar(UUID conversacionId, String nombre);
}
