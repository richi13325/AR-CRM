package com.ar.crm2.whatsapp.application.conversacion.port.out;

import com.ar.crm2.whatsapp.domain.entity.Conversacion;

import java.time.LocalDateTime;
import java.util.List;

public interface FindConversacionesInactivasPort {
    /** Conversaciones no CERRADAs cuyo último mensaje es anterior a {@code limite}. */
    List<Conversacion> findInactivasDesde(LocalDateTime limite);
}
