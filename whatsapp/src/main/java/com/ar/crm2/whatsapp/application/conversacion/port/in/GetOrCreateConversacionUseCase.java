package com.ar.crm2.whatsapp.application.conversacion.port.in;

import com.ar.crm2.whatsapp.domain.entity.Conversacion;

import java.util.UUID;

public interface GetOrCreateConversacionUseCase {
    Conversacion getOrCreate(UUID canalId, String numeroTelefono, String nombreContacto);
}
