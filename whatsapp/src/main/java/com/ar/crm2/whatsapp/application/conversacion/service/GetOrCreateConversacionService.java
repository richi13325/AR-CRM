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
                .orElseGet(() -> crearOReusar(canal, numeroTelefono, nombreContacto));
    }

    // Dos mensajes del mismo contacto que lleguen casi a la vez pueden pasar
    // ambos el find inicial e intentar crear la conversación. El índice UNIQUE
    // (numero_telefono, canal_id) en BD hace que el segundo save falle; en ese
    // caso re-buscamos y devolvemos la que ganó la carrera, en vez de propagar
    // el error. Si la re-búsqueda no la encuentra, el fallo fue por otra causa.
    private Conversacion crearOReusar(CanalWhatsappId canal, String numeroTelefono, String nombreContacto) {
        try {
            return savePort.save(Conversacion.create(canal, numeroTelefono, nombreContacto));
        } catch (RuntimeException e) {
            return findPort.findByTelefonoAndCanal(numeroTelefono, canal)
                    .orElseThrow(() -> e);
        }
    }
}
