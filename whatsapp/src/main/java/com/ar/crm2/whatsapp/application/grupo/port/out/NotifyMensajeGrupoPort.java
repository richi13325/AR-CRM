package com.ar.crm2.whatsapp.application.grupo.port.out;

import com.ar.crm2.whatsapp.domain.entity.MensajeGrupo;

/** Empuja por SSE un mensaje nuevo de grupo a los clientes conectados. */
public interface NotifyMensajeGrupoPort {
    void notifyGrupo(MensajeGrupo mensaje);
}
