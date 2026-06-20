package com.ar.crm2.whatsapp.application.ia.port.in;

import java.util.UUID;

/** Sugiere una respuesta para una conversación a partir de su historial reciente. */
public interface SugerirRespuestaUseCase {
    String sugerir(UUID conversacionId);
}
