package com.ar.crm2.whatsapp.application.mensaje.port.out;

import com.ar.crm2.whatsapp.domain.entity.Mensaje;

public interface SaveMensajePort {
    Mensaje save(Mensaje mensaje);
}
