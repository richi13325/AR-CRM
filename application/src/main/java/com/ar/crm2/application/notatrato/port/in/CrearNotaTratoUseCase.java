package com.ar.crm2.application.notatrato.port.in;

import com.ar.crm2.model.entity.NotaTrato;

import java.util.UUID;

public interface CrearNotaTratoUseCase {
    NotaTrato crear(UUID tratoId, UUID autorId, String contenido);
}
