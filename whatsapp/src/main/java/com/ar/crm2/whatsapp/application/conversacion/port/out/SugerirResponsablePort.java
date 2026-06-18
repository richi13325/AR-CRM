package com.ar.crm2.whatsapp.application.conversacion.port.out;

import com.ar.crm2.model.vo.UsuarioId;

import java.util.Optional;

/** Sugiere el agente activo con menos conversaciones asignadas (round-robin por carga). */
public interface SugerirResponsablePort {
    Optional<UsuarioId> sugerirMenosCargado();
}
