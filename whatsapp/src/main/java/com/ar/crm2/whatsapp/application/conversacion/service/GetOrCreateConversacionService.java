package com.ar.crm2.whatsapp.application.conversacion.service;

import com.ar.crm2.whatsapp.application.conversacion.port.in.GetOrCreateConversacionUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.out.FindConversacionByTelefonoYCanalPort;
import com.ar.crm2.whatsapp.application.conversacion.port.out.SaveConversacionPort;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class GetOrCreateConversacionService implements GetOrCreateConversacionUseCase {

    private final FindConversacionByTelefonoYCanalPort findPort;
    private final SaveConversacionPort savePort;

    @Override
    public Conversacion getOrCreate(UUID canalId, String numeroTelefono, String nombreContacto) {
        CanalWhatsappId canal = CanalWhatsappId.from(canalId);
        return findPort.findByTelefonoAndCanal(numeroTelefono, canal)
                .orElseGet(() -> savePort.save(
                        Conversacion.create(canal, numeroTelefono, nombreContacto)
                ));
    }
}
