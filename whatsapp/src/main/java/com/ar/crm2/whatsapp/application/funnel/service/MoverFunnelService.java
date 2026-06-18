package com.ar.crm2.whatsapp.application.funnel.service;

import com.ar.crm2.whatsapp.application.conversacion.port.out.FindConversacionByIdPort;
import com.ar.crm2.whatsapp.application.funnel.port.in.MoverFunnelUseCase;
import com.ar.crm2.whatsapp.application.funnel.port.out.MoverContactoAEtapaPort;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class MoverFunnelService implements MoverFunnelUseCase {

    private final FindConversacionByIdPort findConversacionPort;
    private final MoverContactoAEtapaPort moverPort;

    @Override
    public boolean mover(UUID conversacionId, String nombreEtapa) {
        Conversacion conversacion = findConversacionPort.findById(ConversacionId.from(conversacionId))
                .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada: " + conversacionId));
        if (conversacion.getContactoId() == null) return false;
        return moverPort.mover(conversacion.getContactoId(), nombreEtapa);
    }
}
