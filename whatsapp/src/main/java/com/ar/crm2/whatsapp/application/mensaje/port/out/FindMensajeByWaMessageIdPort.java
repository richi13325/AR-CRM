package com.ar.crm2.whatsapp.application.mensaje.port.out;

import com.ar.crm2.whatsapp.domain.entity.Mensaje;

import java.util.Optional;

public interface FindMensajeByWaMessageIdPort {
    Optional<Mensaje> findByWaMessageId(String waMessageId);
}
