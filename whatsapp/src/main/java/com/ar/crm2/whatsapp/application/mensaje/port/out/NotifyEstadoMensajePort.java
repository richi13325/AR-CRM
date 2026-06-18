package com.ar.crm2.whatsapp.application.mensaje.port.out;

import com.ar.crm2.whatsapp.domain.entity.Mensaje;

/** Empuja por SSE el cambio de estado (enviado/entregado/leído) de un mensaje saliente. */
public interface NotifyEstadoMensajePort {
    void notifyEstado(Mensaje mensaje);
}
