package com.ar.crm2.whatsapp.application.conversacion.service;

import com.ar.crm2.whatsapp.application.conversacion.port.in.MarcarConversacionLeidaUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.out.FindConversacionByIdPort;
import com.ar.crm2.whatsapp.application.conversacion.port.out.SaveConversacionPort;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class MarcarConversacionLeidaService implements MarcarConversacionLeidaUseCase {

    private final FindConversacionByIdPort findPort;
    private final SaveConversacionPort savePort;

    @Override
    public Conversacion marcarLeida(UUID conversacionId) {
        Conversacion conv = findPort.findById(ConversacionId.from(conversacionId))
                .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada: " + conversacionId));
        return savePort.save(conv.marcarLeido());
    }
}
