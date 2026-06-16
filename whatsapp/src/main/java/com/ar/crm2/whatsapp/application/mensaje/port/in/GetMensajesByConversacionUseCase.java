package com.ar.crm2.whatsapp.application.mensaje.port.in;

import com.ar.crm2.whatsapp.domain.entity.Mensaje;

import java.util.List;
import java.util.UUID;

public interface GetMensajesByConversacionUseCase {
    List<Mensaje> getByConversacion(UUID conversacionId);
}
