package com.ar.crm2.whatsapp.application.mensaje.service;

import com.ar.crm2.whatsapp.application.mensaje.port.in.GetMensajesByConversacionUseCase;
import com.ar.crm2.whatsapp.application.mensaje.port.out.FindMensajesByConversacionIdPort;
import com.ar.crm2.whatsapp.domain.entity.Mensaje;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class GetMensajesByConversacionService implements GetMensajesByConversacionUseCase {

    private final FindMensajesByConversacionIdPort findPort;

    @Override
    public List<Mensaje> getByConversacion(UUID conversacionId) {
        return findPort.findByConversacionId(ConversacionId.from(conversacionId));
    }
}
