package com.ar.crm2.whatsapp.application.conversacion.service;

import com.ar.crm2.model.vo.UsuarioId;
import com.ar.crm2.whatsapp.application.conversacion.command.AsignarAgenteCommand;
import com.ar.crm2.whatsapp.application.conversacion.port.in.AsignarAgenteUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.out.FindConversacionByIdPort;
import com.ar.crm2.whatsapp.application.conversacion.port.out.SaveConversacionPort;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AsignarAgenteService implements AsignarAgenteUseCase {

    private final FindConversacionByIdPort findPort;
    private final SaveConversacionPort savePort;

    @Override
    public Conversacion asignar(AsignarAgenteCommand command) {
        Conversacion conversacion = findPort.findById(ConversacionId.from(command.conversacionId()))
                .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada: " + command.conversacionId()));

        return savePort.save(conversacion.asignarAgente(UsuarioId.from(command.usuarioId())));
    }
}
