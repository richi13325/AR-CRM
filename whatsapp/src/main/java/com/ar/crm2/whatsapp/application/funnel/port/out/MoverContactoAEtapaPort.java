package com.ar.crm2.whatsapp.application.funnel.port.out;

import com.ar.crm2.model.vo.ContactoId;

/**
 * Mueve la oportunidad (Trato) del contacto a la columna del tablero de TRATOS
 * cuyo nombre coincide con {@code nombreEtapa} (case-insensitive).
 */
public interface MoverContactoAEtapaPort {
    /** @return true si se movió; false si el contacto no tiene una oportunidad activa o la etapa no existe. */
    boolean mover(ContactoId contactoId, String nombreEtapa);
}
