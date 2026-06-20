package com.ar.crm2.application.trato.port.in;

import com.ar.crm2.model.entity.Trato;

import java.util.UUID;

/**
 * Cierra una oportunidad: marcarla como ganada o perdida (con motivo).
 */
public interface CambiarEstadoTratoUseCase {
    Trato ganar(UUID id);
    Trato perder(UUID id, String motivo);
}
