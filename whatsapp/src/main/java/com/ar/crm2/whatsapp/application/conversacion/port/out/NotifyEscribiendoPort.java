package com.ar.crm2.whatsapp.application.conversacion.port.out;

import com.ar.crm2.whatsapp.domain.vo.ConversacionId;

public interface NotifyEscribiendoPort {
    void notificarEscribiendo(ConversacionId conversacionId);
}
