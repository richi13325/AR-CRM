package com.ar.crm2.whatsapp.application.conversacion.service;

import com.ar.crm2.whatsapp.application.conversacion.port.in.AutoResolverConversacionesUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.out.FindConversacionesInactivasPort;
import com.ar.crm2.whatsapp.application.conversacion.port.out.SaveConversacionPort;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.enums.EstadoConversacion;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class AutoResolverConversacionesService implements AutoResolverConversacionesUseCase {

    private final FindConversacionesInactivasPort findInactivasPort;
    private final SaveConversacionPort savePort;

    @Override
    public int resolverInactivas(int horasInactividad) {
        LocalDateTime limite = LocalDateTime.now().minusHours(horasInactividad);
        var inactivas = findInactivasPort.findInactivasDesde(limite);
        for (Conversacion c : inactivas) {
            savePort.save(c.cambiarEstado(EstadoConversacion.CERRADA));
        }
        return inactivas.size();
    }
}
