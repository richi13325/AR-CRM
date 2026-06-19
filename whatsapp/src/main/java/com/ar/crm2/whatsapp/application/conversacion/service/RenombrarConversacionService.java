package com.ar.crm2.whatsapp.application.conversacion.service;

import com.ar.crm2.whatsapp.application.canal.port.out.UpsertContactoPort;
import com.ar.crm2.whatsapp.application.conversacion.port.in.RenombrarConversacionUseCase;
import com.ar.crm2.whatsapp.application.conversacion.port.out.FindConversacionByIdPort;
import com.ar.crm2.whatsapp.application.conversacion.port.out.SaveConversacionPort;
import com.ar.crm2.whatsapp.domain.entity.Conversacion;
import com.ar.crm2.whatsapp.domain.vo.ConversacionId;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class RenombrarConversacionService implements RenombrarConversacionUseCase {

    private final FindConversacionByIdPort findPort;
    private final SaveConversacionPort savePort;
    private final UpsertContactoPort upsertContactoPort;

    @Override
    public Conversacion renombrar(UUID conversacionId, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        Conversacion conv = findPort.findById(ConversacionId.from(conversacionId))
                .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada: " + conversacionId));

        Conversacion renombrada = savePort.save(conv.conNombre(nombre.trim()));
        // Mantiene sincronizado el Contacto del CRM (si la conversación está vinculada).
        if (conv.getContactoId() != null) {
            upsertContactoPort.actualizarNombre(conv.getContactoId(), nombre.trim());
        }
        return renombrada;
    }
}
