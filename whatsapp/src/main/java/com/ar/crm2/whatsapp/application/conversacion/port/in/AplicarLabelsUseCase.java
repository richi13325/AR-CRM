package com.ar.crm2.whatsapp.application.conversacion.port.in;

import com.ar.crm2.whatsapp.domain.entity.Conversacion;

import java.util.Set;
import java.util.UUID;

/** Contrato Chatwoot/n8n: el bot manda la lista completa de labels (reemplaza, no agrega). */
public interface AplicarLabelsUseCase {
    Conversacion aplicar(UUID conversacionId, Set<String> labels);
}
