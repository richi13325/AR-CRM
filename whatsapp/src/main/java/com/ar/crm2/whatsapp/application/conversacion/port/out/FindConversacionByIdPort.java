package com.ar.crm2.whatsapp.application.conversacion.port.out;

import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;

import java.util.Optional;

public interface FindConversacionByIdPort {
    Optional<Conversacion> findById(ConversacionId id);
}
