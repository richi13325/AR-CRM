package com.ar.crm2.whatsapp.application.mensaje.port.out;

import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;

import java.util.List;

public interface FindMensajesByConversacionIdPort {
    List<Mensaje> findByConversacionId(ConversacionId conversacionId);
}
