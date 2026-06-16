package com.ar.crm2.whatsapp.application.conversacion.port.out;

import com.ar.crm2.whatsapp.domain.entity.Conversacion;

public interface SaveConversacionPort {
    Conversacion save(Conversacion conversacion);
}
