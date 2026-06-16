package com.ar.crm2.whatsapp.application.conversacion.service;

import com.ar.crm2.whatsapp.application.conversacion.port.in.GetConversacionByIdUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.out.FindConversacionByIdPort;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class GetConversacionByIdService implements GetConversacionByIdUseCase {

    private final FindConversacionByIdPort findPort;

    @Override
    public Conversacion getById(UUID conversacionId) {
        return findPort.findById(ConversacionId.from(conversacionId))
                .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada: " + conversacionId));
    }
}
